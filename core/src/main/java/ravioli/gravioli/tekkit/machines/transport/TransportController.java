package ravioli.gravioli.tekkit.machines.transport;

import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class TransportController {
    private Pipe container;
    protected List<BlockFace> faces = new ArrayList(Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN));

    public TransportController(Pipe container) {
        this.container = container;
    }

    public Pipe getContainer() {
        return container;
    }

    abstract void addItem(ItemStack item, BlockFace input);
}
