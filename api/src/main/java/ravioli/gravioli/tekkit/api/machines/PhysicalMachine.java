package ravioli.gravioli.tekkit.api.machines;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.util.List;


public interface PhysicalMachine extends Machine {
    int getId();

    List<ItemStack> getDrops();

    Location getLocation();

    Block getBlock();

    World getWorld();

    void place(Player player, Location location);

    void destroy(boolean drop);

    void save();

    void load(ResultSet result);

    void delete();

    /**
     * Determines whether or not the player can interact with the base block that the machine uses.
     *
     * @return if the machine's vanilla block can be interacted with
     */
    boolean canInteract();
}
