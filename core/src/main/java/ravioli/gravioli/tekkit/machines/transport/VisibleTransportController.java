package ravioli.gravioli.tekkit.machines.transport;

import org.bukkit.Location;
import org.bukkit.block.*;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import ravioli.gravioli.tekkit.api.TekkitAPI;
import ravioli.gravioli.tekkit.api.machines.PhysicalMachine;
import ravioli.gravioli.tekkit.machines.utils.MachineUtils;

import java.util.*;
import java.util.stream.Collectors;

public class VisibleTransportController extends TransportController {
    public VisibleTransportController(Pipe container) {
        super(container);
    }

    @Override
    public void addItem(ItemStack item, BlockFace input) {
        TransportItem transportItem = new TransportItem(item, getContainer().getLocation(), input);
        addItem(transportItem, input);
    }

    public TransportItemSet getItemSet() {
        return getContainer().itemSet;
    }

    public Collection<ItemStack> getDrops() {
        return getItemSet().stream().map(TransportItem::getItemStack).collect(Collectors.toList());
    }

    public void destroy() {
        getItemSet().forEach(TransportItem::destroy);
    }

    public void addItem(TransportItem item, BlockFace input) {
        getItemSet().add(item);

        item.input = input;
        item.output = getDestination(item);
        item.setInputPosition(getContainer().getLocation(), input);

        if (item.output == null) {
            item.output = input.getOppositeFace();
        }
    }

    public void update() {
        Iterator<TransportItem> iterator = getItemSet().iterator();
        while (iterator.hasNext()) {
            TransportItem transportItem = iterator.next();
            if (transportItem.output == null) {
                continue;
            }
            Location mid = getContainer().getLocation().clone().add(0.5, 0.5, 0.5);
            MachineUtils.normalizeLocation(mid, 2.0);

            Location destination = mid.clone();
            destination.add(transportItem.output.getModX() * 0.5, transportItem.output.getModY() * 0.5, transportItem.output.getModZ() * 0.5);
            MachineUtils.normalizeLocation(destination, 20.0);

            double factor = getContainer().getSpeed();
            Location loc = transportItem.getLocationClone();
            Location location = !transportItem.reachedCenter?
                    loc.add(transportItem.input.getModX() * -factor, transportItem.input.getModY() * -factor, transportItem.input.getModZ() * -factor) :
                    loc.add(transportItem.output.getModX() * factor, transportItem.output.getModY() * factor, transportItem.output.getModZ() * factor);
            MachineUtils.normalizeLocation(location, 20.0);

            transportItem.setAbsolutePosition(location);
            if ((mid.distance(location) < 0.1 || outOfBounds(transportItem)) && !transportItem.reachedCenter) {
                transportItem.reachedCenter = true;
                MachineUtils.normalizeLocation(location, 2.0);
            }
            if ((endReached(transportItem) || outOfBounds(transportItem)) && transportItem.reachedCenter) {
                iterator.remove();

                Block block = getContainer().getBlock().getRelative(transportItem.output);
                boolean success = injectItem(transportItem, block);

                if (!success) {
                    transportItem.destroy();
                    block.getWorld().dropItem(location, transportItem.getItemStack());
                }
            }
        }
    }

    /**
     * Inject an item into the proper machine or container
     *
     * @param item the transport item
     * @param block the block to test
     *
     * @return successful injection
     */
    public boolean injectItem(TransportItem item, Block block) {
        ItemStack itemStack = item.getItemStack();

        PhysicalMachine machine = TekkitAPI.getMachineManager().getMachine(block.getLocation());
        if (machine != null) {
            if (machine instanceof Pipe) {
                passItem(item, (Pipe) machine);
                return true;
            } else if (machine instanceof TransportReceiver) {
                TransportReceiver receiver = (TransportReceiver) machine;
                receiver.addItem(item.getItemStack(), item.output.getOppositeFace());
                item.destroy();
                return true;
            }
            return false;
        }

        if (block.getState() instanceof InventoryHolder) {
            InventoryHolder inventoryBlock = (InventoryHolder) block.getState();

            if (inventoryBlock instanceof Hopper || inventoryBlock instanceof BrewingStand || inventoryBlock instanceof Beacon) {
                return false;
            }

            if (inventoryBlock instanceof Furnace) {
                Furnace furnace = (Furnace) inventoryBlock;

                ItemStack furnaceItem = null;
                Boolean smelting = null;

                if (item.output == BlockFace.UP) {
                    furnaceItem = furnace.getInventory().getFuel();
                    smelting = false;
                } else if (item.output == BlockFace.DOWN) {
                    furnaceItem = furnace.getInventory().getSmelting();
                    smelting = true;
                }
                if (furnaceItem != null) {
                    if (furnaceItem.isSimilar(itemStack)) {
                        int total = furnaceItem.getAmount() + itemStack.getAmount();
                        if (total <= furnaceItem.getMaxStackSize()) {
                            furnaceItem.setAmount(total);
                            item.destroy();
                            return true;
                        }
                        furnaceItem.setAmount(furnaceItem.getMaxStackSize());
                        itemStack.setAmount(total - furnaceItem.getMaxStackSize());
                    }
                } else if (smelting != null) {
                    if (smelting) {
                        furnace.getInventory().setSmelting(itemStack);
                    } else {
                        furnace.getInventory().setFuel(itemStack);
                    }
                    item.destroy();
                    return true;
                }
            } else {
                ItemStack leftover = inventoryBlock.getInventory().addItem(itemStack).get(0);
                if (leftover == null) {
                    item.destroy();
                    return true;
                }
                itemStack.setAmount(leftover.getAmount());
            }
        }
        return false;
    }

    /**
     * Pass a transport item to the specified pipe
     *
     * @param item the item to pass
     * @param pipe the pipe to pass to
     */
    public void passItem(TransportItem item, Pipe pipe) {
        BlockFace input = item.output.getOppositeFace();
        item.reset();
        pipe.addTransportItem(item, input);
    }

    public BlockFace getDestination(TransportItem item) {
        List<BlockFace> results = new ArrayList();

        for (BlockFace face : faces) {
            if (getContainer().acceptableOutput(face) && face != item.input) {
                Block block = getContainer().getBlock().getRelative(face);
                if (item.canInsertItem(block, face.getOppositeFace())) {
                    results.add(face);
                }
            }
        }
        if (results.isEmpty()) {
            return null;
        }
        BlockFace result = results.get(0);
        Collections.rotate(faces, -1 - faces.indexOf(result));

        return result;
    }

    private boolean endReached(TransportItem item) {
        Location destination = getContainer().getLocation().clone().add(0.5, 0.5, 0.5);
        destination.add(item.output.getModX() * 0.5, item.output.getModY() * 0.5, item.output.getModZ() * 0.5);
        MachineUtils.normalizeLocation(destination, 20.0);

        return item.getLocation().distance(destination) < 0.01;
    }

    private boolean outOfBounds(TransportItem item) {
        Location mid = getContainer().getLocation().clone().add(0.5, 0.5, 0.5);
        MachineUtils.normalizeLocation(mid, 2.0);

        return item.getLocation().distance(mid) > 0.6;
    }
}
