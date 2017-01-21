package ravioli.gravioli.tekkit.api.machines;

import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public interface InventoryMachine extends InventoryHolder {
    /**
     * Add items to the machines inventory.
     *
     * @param items the items to add
     * @return collection of items that didn't fit
     */
    Collection<ItemStack> addItems(ItemStack... items);

    /**
     * Whether or not the player can open the machines inventory by right clicking the machine
     *
     * @return if the inventory is opened by right clicking
     */
    boolean interactToOpen();
}
