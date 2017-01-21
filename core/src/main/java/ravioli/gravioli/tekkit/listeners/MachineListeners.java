package ravioli.gravioli.tekkit.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.ItemStack;
import ravioli.gravioli.tekkit.Tekkit;
import ravioli.gravioli.tekkit.api.TekkitAPI;
import ravioli.gravioli.tekkit.api.machines.InventoryMachine;
import ravioli.gravioli.tekkit.api.machines.PhysicalMachine;

import java.util.Iterator;

public class MachineListeners implements Listener {
    private Tekkit plugin;

    public MachineListeners(Tekkit plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        for (PhysicalMachine machine : plugin.getMachineManager().getUnloadedMachines(event.getWorld().getName())) {
            plugin.getSqlite().loadMachine(machine);
        }
    }

    @EventHandler
    public void onWorldSave(WorldSaveEvent event) {
        Iterator<PhysicalMachine> machines = plugin.getMachineManager().getMachinesMarkedForDeletion().iterator();
        while (machines.hasNext()) {
            PhysicalMachine machine = machines.next();
            if (machine.getWorld().equals(event.getWorld())) {
                machine.delete();
                machines.remove();
            }
        }
        for (PhysicalMachine machine : plugin.getMachineManager().getMachines()) {
            machine.save();
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            Player player = event.getPlayer();
            if (TekkitAPI.getMachineManager().isMachine(block.getLocation())) {
                ItemStack item = event.getItem();
                if (item == null || (item != null && !item.getType().isBlock() &&
                        item.getType() != Material.REDSTONE || !player.isSneaking())) {
                    PhysicalMachine machine = TekkitAPI.getMachineManager().getMachine(block.getLocation());
                    if (!machine.canInteract()) {
                        event.setUseInteractedBlock(Event.Result.DENY);
                        event.setCancelled(true);
                    }
                    if (machine instanceof InventoryMachine && ((InventoryMachine) machine).interactToOpen()) {
                        player.openInventory(((InventoryMachine) machine).getInventory());
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }

        ItemStack item = event.getItemInHand();
        if (item != null) {
            PhysicalMachine machine = TekkitAPI.getMachineManager().getMachine(item);
            if (machine != null) {
                try {
                    PhysicalMachine newMachine = machine.getClass().newInstance();
                    newMachine.place(event.getPlayer(), event.getBlock().getLocation());
                } catch(IllegalAccessException | InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Block block = event.getBlock();
        if (TekkitAPI.getMachineManager().isMachine(block.getLocation())) {
            TekkitAPI.getMachineManager().getMachine(block.getLocation()).destroy(true);
        }
    }
}
