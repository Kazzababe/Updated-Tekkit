package ravioli.gravioli.tekkit.machines;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;
import ravioli.gravioli.tekkit.Tekkit;
import ravioli.gravioli.tekkit.api.TekkitAPI;
import ravioli.gravioli.tekkit.api.TekkitPlugin;
import ravioli.gravioli.tekkit.api.machines.InventoryMachine;
import ravioli.gravioli.tekkit.api.machines.PhysicalMachine;
import ravioli.gravioli.tekkit.database.DatabaseObject;
import ravioli.gravioli.tekkit.database.Sqlite;
import ravioli.gravioli.tekkit.database.utils.DatabaseUtils;
import ravioli.gravioli.tekkit.machines.transport.Pipe;
import ravioli.gravioli.tekkit.machines.transport.TransportItem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public abstract class SimpleMachine implements PhysicalMachine, Listener, Runnable {
    private int id = -1;
    @DatabaseObject
    private UUID owner;
    @DatabaseObject
    private Location location;
    private int taskId;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public List<ItemStack> getDrops() {
        return new ArrayList();
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public Block getBlock() {
        return location.getBlock();
    }

    @Override
    public World getWorld() {
        return location.getWorld();
    }

    public void saveJSON() {
        JSONObject jsonObject = new JSONObject();

        Field[] fields = DatabaseUtils.getAllFields(getClass());
        for (Field field : fields) {
            if (field.getAnnotation(DatabaseObject.class) == null) {
                continue;
            }
            boolean accessibility = field.isAccessible();
            field.setAccessible(true);

            try {
                Class type = field.getType();
                String name = field.getName();
                Object object = field.get(this);

                if (type.isEnum()) {
                    jsonObject.put(name, type.toString());
                } else if (type.isPrimitive() || type.isAssignableFrom(String.class)) {
                    jsonObject.put(name, object);
                } else if (TekkitAPI.getMachineManager().hasSerializer(type)) {
                    jsonObject.put(name, TekkitAPI.getMachineManager().getSerializer(type).serialize(object));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        try (FileWriter file = new FileWriter(TekkitAPI.getInstance().getDataFolder().getAbsolutePath() + File.separator + "tekkit.json")) {
            file.write(jsonObject.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public final void save() {
        boolean inserting = id == -1;
        StringBuilder sql = new StringBuilder(inserting?
                "INSERT INTO `" + getFormattedName() + "` (" :
                "UPDATE `" + getFormattedName() + "` set"
        );

        Map<String, Field> types = new LinkedHashMap();
        Map<String, Boolean> fieldAccessibility = new HashMap();

        Field[] fields = DatabaseUtils.getAllFields(getClass());
        for (Field field : fields) {
            if (field.getAnnotation(DatabaseObject.class) == null) {
                continue;
            }
            boolean accessibility = field.isAccessible();
            field.setAccessible(true);

            String name = field.getName();
            types.put(name, field);
            fieldAccessibility.put(name, accessibility);

            sql.append(inserting?
                "`" + name + "`, " :
                " `" + name + "` = ?,"
            );
        }
        sql.delete(inserting?
            sql.length() - 2 : sql.length() - 1,
            sql.length()
        );
        sql.append(inserting? ") VALUES (" : " WHERE `id` = ?");
        if (inserting) {
            for (int i = 0; i < types.size(); i++) {
                sql.append("?, ");
            }
            sql.delete(sql.length() - 2, sql.length()).append(")");
        }

        Sqlite sqlite = ((Tekkit) TekkitAPI.getInstance()).getSqlite();
        try (PreparedStatement statement = inserting?
            sqlite.getConnection().prepareStatement(sql.toString(), new String[]{"id"}) :
            sqlite.getConnection().prepareStatement(sql.toString())) {
            int count = 1;
            for (Map.Entry<String, Field> entry : types.entrySet()) {
                String name = entry.getKey();
                Field field = entry.getValue();

                Class type = field.getType();
                Object object = field.get(this);

                if (type.isEnum()) {
                    statement.setString(count, object.toString());
                } else if (type.isPrimitive()) {
                    statement.setObject(count, object);
                } else if (TekkitAPI.getMachineManager().hasSerializer(type)) {
                    statement.setString(count, TekkitAPI.getMachineManager().getSerializer(type).serialize(object));
                }
                count++;

                field.setAccessible(fieldAccessibility.get(name));
            }
            if (!inserting) {
                statement.setInt(count, id);
            }
            statement.executeUpdate();

            if (inserting) {
                ResultSet results = statement.getGeneratedKeys();
                if (results.next()) {
                    id = results.getInt(1);
                }
            }
        } catch (SQLException| IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public final void load(JSONObject jsonObject) {
        Field[] fields = DatabaseUtils.getAllFields(getClass());
        for (Field field : fields) {
            if (field.getAnnotation(DatabaseObject.class) == null) {
                continue;
            }
            boolean accessible = field.isAccessible();
            field.setAccessible(true);

            Class type = field.getType();
            String name = field.getName();
            Object object = jsonObject.get(name);

            try {
                if (type.isEnum()) {
                    field.set(this, Enum.valueOf(type, (String) object));
                } else if (type.isPrimitive()) {
                    field.set(this, object);
                } else if (type.isAssignableFrom(String.class)) {
                    field.set(this, (String) object);
                } else if (TekkitAPI.getMachineManager().hasSerializer(type) && !((String) object).isEmpty()) {
                    field.set(this, TekkitAPI.getMachineManager().getSerializer(type).deserialize((String) object));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            field.setAccessible(accessible);
        }
        enable();
    }

    @Override
    public final void load(ResultSet results) {
        Field[] fields = DatabaseUtils.getAllFields(getClass());
        for (Field field : fields) {
            if (field.getAnnotation(DatabaseObject.class) == null) {
                continue;
            }
            boolean accessible = field.isAccessible();
            field.setAccessible(true);

            Class type = field.getType();
            String name = field.getName();

            try {
                if (type.isEnum()) {
                    field.set(this, Enum.valueOf(type, results.getString(name)));
                } else if (type.isPrimitive()) {
                    field.set(this, results.getObject(name));
                } else if (type.isAssignableFrom(String.class)) {
                    field.set(this, results.getString(name));
                } else if (TekkitAPI.getMachineManager().hasSerializer(type) && !results.getString(name).isEmpty()) {
                    field.set(this, TekkitAPI.getMachineManager().getSerializer(type).deserialize(results.getString(name)));
                }
            } catch (SQLException | IllegalAccessException e) {
                e.printStackTrace();
            }
            field.setAccessible(accessible);
        }
        enable();
    }

    @Override
    public void delete() {
        Sqlite sqlite = ((Tekkit) TekkitAPI.getInstance()).getSqlite();
        try (PreparedStatement statement = sqlite.getConnection().prepareStatement(
                "DELETE FROM `" + getFormattedName() + "` WHERE `id` = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public final void register(TekkitPlugin plugin) {
        if (!(plugin instanceof Tekkit)) {
            throw new IllegalArgumentException("plugin must be instance of " + Tekkit.class);
        }
        Tekkit tekkit = (Tekkit) plugin;
        tekkit.getSqlite().createTable(this);
        tekkit.getServer().addRecipe(getRecipe());
    }

    @Override
    public void run() {
        if (getLocation().getChunk().isLoaded()) {
            update();
        }
    }

    public final void startTask(long interval) {
        stopTask();
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(TekkitAPI.getInstance(), this, interval, interval);
    }

    public void stopTask() {
        Bukkit.getScheduler().cancelTask(taskId);
    }

    @Override
    public final void place(Player player, Location location) {
        owner = player != null? player.getUniqueId() : null;
        this.location = location;

        onPlace();
        enable();
    }

    public final void enable() {
        Bukkit.getPluginManager().registerEvents(this, TekkitAPI.getInstance());
        TekkitAPI.getMachineManager().addMachine(this);

        onEnable();
    }

    public final void destroy(boolean drop) {
        if (drop) {
            getDrops().forEach(item -> getWorld().dropItem(location, item));
            getWorld().dropItem(location, getRecipe().getResult());
        }
        if (getBlock().getState() instanceof InventoryHolder) {
            ((InventoryHolder) getBlock().getState()).getInventory().clear();
        }
        getBlock().setTypeIdAndData(0, (byte) 0, true);

        HandlerList.unregisterAll(this);
        onDestroy();

        TekkitAPI.getMachineManager().removeMachine(this);
    }

    protected void onPlace() {
        // Does nothing. Makes implementing the method in other machines non-mandatory
    }

    protected void onEnable() {
        // Does nothing. Makes implementing the method in other machines non-mandatory
    }

    protected void onDestroy() {
        // Does nothing. Makes implementing the method in other machines non-mandatory
    }

    protected void update() {
        // Does nothing. Makes implementing the method in other machines non-mandatory
    }

    public void routeItem(BlockFace output, ItemStack... items) {
        Block blockOutput = getBlock().getRelative(output);
        if (TekkitAPI.getMachineManager().isMachine(blockOutput.getLocation())) {
            List<ItemStack> drops = new ArrayList<ItemStack>();

            PhysicalMachine machine = TekkitAPI.getMachineManager().getMachine(blockOutput.getLocation());
            if (machine instanceof Pipe) {
                Pipe pipe = (Pipe) machine;
                if (pipe.acceptableInput(output.getOppositeFace())) {
                    for (ItemStack item : items) {
                        pipe.addItem(item, output.getOppositeFace());
                    }
                } else {
                    drops.addAll(Arrays.asList(items));
                }
            } else if (machine instanceof InventoryMachine) {
                Collection<ItemStack> leftover = ((InventoryMachine) machine).addItems(items);
                leftover.forEach(drop -> getWorld().dropItem(blockOutput.getLocation(), drop));
            }
            drops.forEach(drop -> getWorld().dropItem(blockOutput.getLocation(), drop));
        } else {
            if (blockOutput.getState() instanceof InventoryHolder) {
                if (blockOutput.getState() instanceof Chest) {
                    Collection<ItemStack> leftover = ((Chest) blockOutput.getState()).getInventory().addItem(items).values();
                    leftover.forEach(drop -> getWorld().dropItem(blockOutput.getLocation(), drop));
                }
            }
        }
    }
}
