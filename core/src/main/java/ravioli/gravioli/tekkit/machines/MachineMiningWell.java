package ravioli.gravioli.tekkit.machines;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import ravioli.gravioli.tekkit.api.TekkitAPI;
import ravioli.gravioli.tekkit.api.machines.InventoryMachine;
import ravioli.gravioli.tekkit.api.machines.PhysicalMachine;
import ravioli.gravioli.tekkit.database.DatabaseObject;
import ravioli.gravioli.tekkit.machines.utils.Fuel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class MachineMiningWell extends SimpleMachine implements InventoryMachine {
    private static final Material DRILL_MATERIAL = Material.COBBLE_WALL;

    private Inventory inventory;

    @DatabaseObject
    private int height;
    @DatabaseObject
    private long fuelDuration;

    @Override
    public void onPlace() {
        // We only need to initialize it on place because when the machine is loaded, it automatically creates it
        inventory = Bukkit.createInventory(null, 9, "Mining Well");
    }

    @Override
    public void onEnable() {
        startTask(20);
        if (height > 0) {
            for (int i = 0; i <= height; i++) {
                Location location = getLocation().clone().subtract(0, i, 0);
                if (location.getBlock().getType() == DRILL_MATERIAL) {
                    location.getBlock().setMetadata("machine", new FixedMetadataValue(TekkitAPI.getInstance(), this));
                }
            }
        }
    }

    private boolean checkForFuel() {
        ArrayList<ItemStack> items = new ArrayList(Arrays.asList(inventory.getContents()));
        ItemStack fuel = items
                .stream()
                .filter(item -> item != null && Fuel.FUELS.containsKey(item.getType()))
                .findFirst()
                .orElse(null);
        if (fuel != null) {
            fuelDuration = (long) (Fuel.FUELS.get(fuel.getType()).getDuration() / 2.5);
            inventory.removeItem(new ItemStack(fuel.getType(), 1, fuel.getDurability()));
            return true;
        }
        return false;
    }

    @Override
    public void update() {
        Location location = getLocation().clone().subtract(0, height + 1, 0);
        if (location.getBlock().getType() != Material.BEDROCK && location.getY() > 0) {
            if (fuelDuration <= 0) {
                if (!checkForFuel()) {
                    return;
                }
            }
            fuelDuration -= 1000;
            height++;

            PhysicalMachine machine = TekkitAPI.getMachineManager().getMachine(location);
            if (machine != null) {
                machine.getDrops().forEach(drop -> routeItem(BlockFace.UP, drop));
                routeItem(BlockFace.UP, machine.getRecipe().getResult());
                machine.destroy(false);
            } else if (!location.getBlock().hasMetadata("machine")) {
                location.getBlock().getDrops().forEach(drop -> routeItem(BlockFace.UP, drop));
            }
            location.getBlock().setType(DRILL_MATERIAL);
            location.getBlock().setMetadata("machine", new FixedMetadataValue(TekkitAPI.getInstance(), this));

            getWorld().playEffect(getLocation(), Effect.SMOKE, 4);
        }
    }

    @Override
    public Recipe getRecipe() {
        ItemStack item = new ItemStack(Material.IRON_BLOCK);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.YELLOW + "Mining Well");
        item.setItemMeta(itemMeta);

        ShapedRecipe recipe = new ShapedRecipe(item);
        recipe.shape("IRI", "ICI", "IPI");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('C', Material.IRON_BLOCK);
        recipe.setIngredient('P', Material.IRON_PICKAXE);

        return recipe;
    }

    @Override
    public String getName() {
        return "mining_well";
    }

    @Override
    public String getFormattedName() {
        return "MiningWell";
    }

    @Override
    public boolean canInteract() {
        return true;
    }

    @Override
    public Collection<ItemStack> addItems(ItemStack... items) {
        return new ArrayList<ItemStack>();
    }

    @Override
    public boolean interactToOpen() {
        return true;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
