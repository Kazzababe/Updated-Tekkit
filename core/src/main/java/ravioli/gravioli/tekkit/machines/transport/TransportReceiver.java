package ravioli.gravioli.tekkit.machines.transport;

import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

public interface TransportReceiver {
    boolean acceptableInput(BlockFace input);

    boolean acceptableOutput(BlockFace ouput);

    boolean canReceiveItem(ItemStack item, BlockFace input);

    void addItem(ItemStack item, BlockFace input);
}
