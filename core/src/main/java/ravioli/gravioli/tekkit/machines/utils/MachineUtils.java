package ravioli.gravioli.tekkit.machines.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MachineUtils {
    public static String locationToString(Location location) {
        return String.format("%s,%s,%s,%s",
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ());
    }

    public static Location stringToLocation(String string) {
        String[] data = string.split(",");
        return new Location(
                Bukkit.getWorld(data[0]),
                Double.parseDouble(data[1]),
                Double.parseDouble(data[2]),
                Double.parseDouble(data[3])
        );
    }

    public static String itemStackArrayToBase64(ItemStack... items) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Write the size of the inventory
            dataOutput.writeInt(items.length);

            // Save every element in the list
            for (int i = 0; i < items.length; i++) {
                dataOutput.writeObject(items[i]);
            }

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    public static ItemStack[] itemStackArrayFromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];

            // Read the serialized inventory
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }

    /**
     * Used to round location coordinates to acceptable values. Mainly used to eliminate inprecise math.
     *
     * @param location location to normalize
     * @param delim delimiter
     */
    public static void normalizeLocation(Location location, double delim) {
        location.setX(Math.round(location.getX() * delim) / delim);
        location.setY(Math.round(location.getY() * delim) / delim);
        location.setZ(Math.round(location.getZ() * delim) / delim);
    }

    /**
     * A super janky method to detect whether or not an item will fit into an inventory
     *
     * @param inventory inventory to test
     * @param items items to check
     * @return if the inventory could fit the specified items
     */
    public static boolean canFitIntoInventory(Inventory inventory, ItemStack... items) {
        Inventory inv = Bukkit.createInventory(null, inventory.getSize());
        inv.setContents(inventory.getContents().clone());

        boolean fits = true;
        for (ItemStack item : items.clone()) {
            int amount = item.getAmount();
            if (!inv.addItem(item).isEmpty()) {
                fits = false;
                item.setAmount(amount);
                break;
            }
        }
        inv = null;

        return fits;
    }

    public static boolean isFuelSource(Material type) {
        switch (type) {
            case LAVA_BUCKET:
            case COAL_BLOCK:
            case BLAZE_ROD:
            case COAL:
            case BOAT:
            case WOOD:
            case LOG:
            case FENCE:
            case FENCE_GATE:
            case WOOD_STAIRS:
            case TRAP_DOOR:
            case WORKBENCH:
            case BOOKSHELF:
            case CHEST:
            case TRAPPED_CHEST:
            case DAYLIGHT_DETECTOR:
            case JUKEBOX:
            case NOTE_BLOCK:
            case BANNER:
            case WOOD_STEP:
            case BOW:
            case FISHING_ROD:
            case LADDER:
            case SIGN:
            case WOOD_DOOR:
            case BOWL:
            case SAPLING:
            case STICK:
            case WOOD_BUTTON:
            case WOOL:
            case CARPET:
            case WOOD_AXE:
            case WOOD_HOE:
            case WOOD_PICKAXE:
            case WOOD_SWORD:
                return true;
        }
        return false;
    }

    public static boolean isSmeltable(Material type) {
        switch (type) {
            case PORK:
            case RAW_BEEF:
            case RAW_CHICKEN:
            case RAW_FISH:
            case POTATO_ITEM:
            case MUTTON:
            case RABBIT:
            case IRON_ORE:
            case GOLD_ORE:
            case SAND:
            case COBBLESTONE:
            case CLAY:
            case NETHERRACK:
            case CLAY_BALL:
            case DIAMOND_ORE:
            case LAPIS_ORE:
            case REDSTONE_ORE:
            case COAL_ORE:
            case EMERALD_ORE:
            case QUARTZ_ORE:
            case LOG:
            case CACTUS:
            case CHORUS_FRUIT:
                return true;
        }
        return false;
    }
}
