package ravioli.gravioli.tekkit.machines.transport;

import org.bukkit.block.*;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ravioli.gravioli.tekkit.Tekkit;
import ravioli.gravioli.tekkit.api.TekkitAPI;
import ravioli.gravioli.tekkit.api.machines.PhysicalMachine;
import ravioli.gravioli.tekkit.machines.transport.utils.TransportUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HiddenTransportController extends TransportController {
    public HiddenTransportController(Pipe container) {
        super(container);
    }

    @Override
    void addItem(ItemStack item, BlockFace input) {
        // This is where we handle literally everything
        processItem(item, input);
    }

    /**
     * Processes the item and figures out where it needs to go and then executes.
     * Note: I'll more than likely clean this up later because it's essentially the same as the visible transport
     * controller
     *
     * @param item the itemstack
     * @param originalInput the original input of the item
     */
    private void processItem(ItemStack item, BlockFace originalInput) {
        BlockFace input = originalInput;
        BlockFace output = getDestination(getContainer().getBlock(), input, item);

        Block current = getContainer().getBlock();
        for (int i = 0; i < Tekkit.MAX_TRANSPORT_BLOCKS; i++) {
            if (output == null) {
                break;
            }
            Block block = current.getRelative(output);
            if (canInsertItem(item, block, output.getOppositeFace())) {
                if (TekkitAPI.getMachineManager().isMachine(block.getLocation())) {
                    PhysicalMachine machine = TekkitAPI.getMachineManager().getMachine(block.getLocation());
                    if (machine instanceof Pipe) {
                        if (machine instanceof VoidPipe) {
                            item = null;
                            return;
                        } else {
                            input = output.getOppositeFace();
                            output = getDestination(block, input, item);
                            current = block;
                            continue;
                        }
                    } else if (machine instanceof TransportReceiver) {
                        TransportReceiver receiver = (TransportReceiver) machine;
                        if (receiver.canReceiveItem(item, output.getOppositeFace())) {
                            receiver.addItem(item, output.getOppositeFace());
                            return;
                        }
                    }
                } else {
                    if (block.getState() instanceof InventoryHolder) {
                        InventoryHolder inventoryBlock = (InventoryHolder) block.getState();

                        if (inventoryBlock instanceof Hopper || inventoryBlock instanceof BrewingStand || inventoryBlock instanceof Beacon) {
                            // Should never reach, but if it does break out of the loop (drop the item)
                            break;
                        }

                        if (inventoryBlock instanceof Furnace) {
                            Furnace furnace = (Furnace) inventoryBlock;

                            ItemStack furnaceItem = null;
                            Boolean smelting = null;

                            if (output == BlockFace.UP) {
                                furnaceItem = furnace.getInventory().getFuel();
                                smelting = false;
                            } else if (output == BlockFace.DOWN) {
                                furnaceItem = furnace.getInventory().getSmelting();
                                smelting = true;
                            }
                            if (furnaceItem != null) {
                                if (furnaceItem.isSimilar(item)) {
                                    int total = furnaceItem.getAmount() + item.getAmount();
                                    if (total <= furnaceItem.getMaxStackSize()) {
                                        furnaceItem.setAmount(total);
                                        return;
                                    }
                                    furnaceItem.setAmount(furnaceItem.getMaxStackSize());
                                    item.setAmount(total - furnaceItem.getMaxStackSize());
                                }
                            } else if (smelting != null) {
                                if (smelting) {
                                    furnace.getInventory().setSmelting(item);
                                } else {
                                    furnace.getInventory().setFuel(item);
                                }
                                return;
                            }
                        } else {
                            ItemStack leftover = inventoryBlock.getInventory().addItem(item).get(0);
                            if (leftover == null) {
                                return;
                            }
                            item.setAmount(leftover.getAmount());
                        }
                        break;
                    }
                }
            }
        }

        if (item != null && item.getAmount() > 0) {
            getContainer().getWorld().dropItem(current.getLocation(), item);
        }
    }

    public BlockFace getDestination(Block container, BlockFace input, ItemStack item) {
        List<BlockFace> results = new ArrayList();
        PhysicalMachine machine = TekkitAPI.getMachineManager().getMachine(container.getLocation());

        List<BlockFace> allFaces = new ArrayList(faces);
        if (machine != null && machine instanceof Pipe) {
            allFaces = new ArrayList(((Pipe) machine).transportController.faces);
        }
        for (BlockFace face : allFaces) {
            if (face != input) {
                if ((machine != null && machine instanceof Pipe && ((Pipe) machine).acceptableOutput(face)) || machine == null) {
                    Block block = container.getRelative(face);
                    if (canInsertItem(item, block, face.getOppositeFace())) {
                        results.add(face);
                    }
                }
            }
        }
        if (results.isEmpty()) {
            return null;
        }
        BlockFace result = results.get(0);
        if (machine != null && machine instanceof Pipe) {
            Collections.rotate(((Pipe) machine).transportController.faces, -1 - ((Pipe) machine).transportController.faces.indexOf(result));
        }

        return result;
    }

    /**
     * Essentially the same as {@link TransportItem#canInsertItem(Block, BlockFace)}, without the required TransportItem
     *
     * @param item the item you're inserting
     * @param block block containing the item
     * @param input input of the item
     * @return whether the item can go into the block
     */
    private boolean canInsertItem(ItemStack item, Block block, BlockFace input) {
        PhysicalMachine machine = TekkitAPI.getMachineManager().getMachine(block.getLocation());
        if (machine != null) {
            if (machine instanceof TransportReceiver) {
                TransportReceiver receiver = (TransportReceiver) machine;
                return receiver.canReceiveItem(item, input);
            }
            return false;
        }

        if (block.getState() instanceof InventoryHolder) {
            InventoryHolder inventoryHolder = (InventoryHolder) block.getState();
            return TransportUtils.canInventoryBlockReceive(inventoryHolder, item, input);
        }
        return false;
    }
}
