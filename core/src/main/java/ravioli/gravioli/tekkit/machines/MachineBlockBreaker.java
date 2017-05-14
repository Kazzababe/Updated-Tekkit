package ravioli.gravioli.tekkit.machines;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dispenser;
import ravioli.gravioli.tekkit.api.TekkitAPI;
import ravioli.gravioli.tekkit.api.machines.PhysicalMachine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class MachineBlockBreaker extends SimpleMachine {
    @Override
    public void update() {
        Dispenser dispenser = (Dispenser) getBlock().getState().getData();
        Block facing = getBlock().getRelative(dispenser.getFacing());
        BlockFace outputFace = dispenser.getFacing().getOppositeFace();

        // If the block is a machine, break it and grab it's contents
        // If the block is an inventory block, break it and grabs it's contents
        // If the block is a regular block, just break it and grab it's drops

        if (TekkitAPI.getMachineManager().isMachine(facing.getLocation())) {
            System.out.println("break machine");
            PhysicalMachine machine = TekkitAPI.getMachineManager().getMachine(facing.getLocation());
            Collection<ItemStack> drops = machine.getDrops();
            drops.forEach(drop -> routeItem(outputFace, drop));
            routeItem(outputFace, machine.getRecipe().getResult());
            machine.destroy(false);
        } else if (facing.getType() != Material.AIR) {
            System.out.println("break not air");
            if (facing.getState() instanceof InventoryHolder) {
                Collection<ItemStack> contents = Arrays.asList(((InventoryHolder) facing.getState()).getInventory().getContents()).stream().filter(Objects::nonNull).collect(Collectors.toList());
                contents.forEach(drop -> routeItem(outputFace, drop));

                ((InventoryHolder) facing.getState()).getInventory().clear();
            }
            Collection<ItemStack> drops = facing.getDrops();
            System.out.println(drops);
            drops.forEach(drop -> routeItem(outputFace, drop));

            facing.setType(Material.AIR);
        }
    }

    @Override
    public void onPlace() {
        // In order for the block dispense event to still be fired, it needs to have something in it
        org.bukkit.block.Dispenser block = (org.bukkit.block.Dispenser) getBlock().getState();
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
        return "block_breaker";
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
