package ravioli.gravioli.tekkit.machines.transport;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import ravioli.gravioli.tekkit.Tekkit;
import ravioli.gravioli.tekkit.api.TekkitAPI;
import ravioli.gravioli.tekkit.api.machines.PhysicalMachine;
import ravioli.gravioli.tekkit.machines.transport.utils.TransportUtils;
import ravioli.gravioli.tekkit.machines.utils.MachineUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TransportItem {
    private Location location;
    private ArmorStand entity;

    public boolean reachedCenter;
    public BlockFace input;
    public BlockFace output;

    public TransportItem(ItemStack item, Location location, BlockFace input) {
        this.location = location.clone().add(0.5, 0.5, 0.5);
        this.location.add(0.5 * input.getModX(), 0.5 * input.getModY(), 0.5 * input.getModZ());

        //I have to add the vertical offset here because teleporting doesn't work (Possible bug?)
        entity = (ArmorStand) location.getWorld().spawnEntity(
                this.location.clone().subtract(0, item.getType().isSolid()? 0.88 : 1.18, 0),
                EntityType.ARMOR_STAND);
        entity.setGravity(false);
        entity.setCustomNameVisible(false);
        entity.setVisible(false);
        entity.setMarker(true);
        entity.setCollidable(false);
        entity.setSmall(true);
        entity.setBasePlate(false);
        entity.setMetadata("display", new FixedMetadataValue(TekkitAPI.getInstance(), this));

        setItemStack(item);
        setInputPosition(location, input);
    }

    public void setInputPosition(Location location, BlockFace input) {
        this.input = input;

        Location start = location.clone().add(0.5, 0.5, 0.5);
        start.add(input.getModX() * 0.5, input.getModY() * 0.5, input.getModZ() * 0.5);
        MachineUtils.normalizeLocation(start, 20.0);

        setAbsolutePosition(start);
    }

    public void setAbsolutePosition(Location location) {
        this.location = location.clone();

        // As far as I can tell, this doesn't actually work for some reason.
        // I'm assuming it has something to do with the way Bukkit/Spigot handles teleporting specifically armour stands
        ItemStack item = getItemStack();
        if (item.getType().isSolid()) {
            entity.teleport(location.clone().subtract(0, 0.88, 0));
        } else {
            entity.teleport(location.clone().subtract(0, 1.18, 0));
        }
    }

    public void setItemStack(ItemStack item) {
        entity.setCustomName(item != null ? item.getType().name() : "AIR");
        entity.setHelmet(item);
    }

    public ItemStack getItemStack() {
        return entity.getHelmet();
    }

    public ArmorStand getEntity() {
        return entity;
    }

    public Location getLocation() {
        return location;
    }

    public Location getLocationClone() {
        return location.clone();
    }

    public void reset() {
        input = null;
        output = null;
        reachedCenter = false;
    }

    public void destroy() {
        entity.remove();
        entity.setHealth(0);
    }

    public boolean canInsertItem(Block block, BlockFace input) {
        PhysicalMachine machine = TekkitAPI.getMachineManager().getMachine(block.getLocation());
        if (machine != null) {
            if (machine instanceof TransportReceiver) {
                TransportReceiver receiver = (TransportReceiver) machine;
                return receiver.canReceiveItem(getItemStack(), input);
            }
            return false;
        }

        if (block.getState() instanceof InventoryHolder) {
            InventoryHolder inventoryHolder = (InventoryHolder) block.getState();
            return TransportUtils.canInventoryBlockReceive(inventoryHolder, getItemStack(), input);
        }
        return false;
    }

    public static TransportItem deserialize(String objectString) {
        Map<String, String> data = new Gson().fromJson(objectString, new TypeToken<Map<String, String>>(){}.getType());

        try {
            ItemStack item = MachineUtils.itemStackArrayFromBase64(data.get("item"))[0];
            Location location = MachineUtils.stringToLocation(data.get("location"));
            BlockFace input = BlockFace.valueOf(data.get("input"));

            TransportItem transportItem = new TransportItem(item, location, input);
            transportItem.output = data.get("output").equals("null")? null : BlockFace.valueOf(data.get("output"));
            transportItem.reachedCenter = Boolean.getBoolean(data.get("reachedCenter"));

            return transportItem;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        Map<String, String> data = new HashMap();
        data.put("block", "wooden pipe");
        data.put("item", getItemStack().getType().name());
        data.put("reachedCenter", reachedCenter + "");
        data.put("input", input.toString());
        data.put("output", output.toString());

        return new Gson().toJson(data);
    }
}
