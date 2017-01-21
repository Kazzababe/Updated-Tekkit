package ravioli.gravioli.tekkit.machines;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

public class MachineBlockBreaker extends SimpleMachine {
    @Override
    public void onPlace() {
        org.bukkit.block.Dispenser block = (org.bukkit.block.Dispenser) this.getLocation().getBlock().getState();
        block.getInventory().setItem(4, new ItemStack(Material.PAPER));
    }

    @Override
    public Recipe getRecipe() {
        ItemStack item = new ItemStack(Material.DISPENSER);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.YELLOW + "Block Breaker");
        item.setItemMeta(itemMeta);

        ShapedRecipe recipe = new ShapedRecipe(item);
        recipe.shape("CIC", "CPC", "CRC");
        recipe.setIngredient('C', Material.COBBLESTONE);
        recipe.setIngredient('I', Material.IRON_PICKAXE);
        recipe.setIngredient('P', Material.PISTON_BASE);
        recipe.setIngredient('R', Material.REDSTONE);

        return recipe;
    }

    @Override
    public String getName() {
        return "blockbreaker";
    }

    @Override
    public String getFormattedName() {
        return "BlockBreaker";
    }

    @EventHandler
    public void onDispenserActivate(BlockDispenseEvent event) {
        Block block = event.getBlock();
        if (block.getLocation().equals(getLocation())) {
            event.setCancelled(true);
            run();
        }
    }

    @Override
    public boolean canInteract() {
        return false;
    }
}
