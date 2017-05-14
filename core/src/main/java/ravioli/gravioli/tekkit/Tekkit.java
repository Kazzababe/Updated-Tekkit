package ravioli.gravioli.tekkit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ravioli.gravioli.tekkit.api.TekkitAPI;
import ravioli.gravioli.tekkit.api.TekkitPlugin;
import ravioli.gravioli.tekkit.api.machines.Machine;
import ravioli.gravioli.tekkit.api.machines.PhysicalMachine;
import ravioli.gravioli.tekkit.commands.Command;
import ravioli.gravioli.tekkit.commands.GiveMachineCommand;
import ravioli.gravioli.tekkit.database.JSONDatabase;
import ravioli.gravioli.tekkit.database.Sqlite;
import ravioli.gravioli.tekkit.machines.MachineBlockBreaker;
import ravioli.gravioli.tekkit.listeners.MachineListeners;
import ravioli.gravioli.tekkit.machines.MachineMiningWell;
import ravioli.gravioli.tekkit.machines.serializers.LocationSerializer;
import ravioli.gravioli.tekkit.machines.serializers.TransportItemSetSerializer;
import ravioli.gravioli.tekkit.machines.serializers.UUIDSerializer;
import ravioli.gravioli.tekkit.machines.transport.*;
import ravioli.gravioli.tekkit.manager.TekkitMachineManager;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.UUID;

public class Tekkit extends JavaPlugin implements TekkitPlugin {
    private TekkitMachineManager machineManager;
    private Sqlite sqlite;
    private JSONDatabase jsonDatabase;

    public static boolean AUTO_EQUIP;
    public static boolean VISIBLE_TRANSPORT;
    public static int MAX_TRANSPORT_BLOCKS;

    @Override
    public void onLoad() {
        sqlite = new Sqlite(this);
        jsonDatabase = new JSONDatabase(this);
    }

    @Override
    public void onEnable() {
        setupConfig();

        TekkitAPI.setInstance(this);
        machineManager = (TekkitMachineManager) TekkitAPI.createMachineManager(this);

        machineManager.registerSerializer(UUID.class, new UUIDSerializer());
        machineManager.registerSerializer(Location.class, new LocationSerializer());
        machineManager.registerSerializer(TransportItemSet.class, new TransportItemSetSerializer());

        machineManager.registerMachine(new MachineBlockBreaker());
        machineManager.registerMachine(new MachineMiningWell());
        machineManager.registerMachine(new WoodenPipe());
        machineManager.registerMachine(new IronPipe());
        machineManager.registerMachine(new GoldPipe());
        machineManager.registerMachine(new DiamondPipe());
        machineManager.registerMachine(new VoidPipe());

        registerCommand(new GiveMachineCommand(this));

        registerListener(new MachineListeners(this));
    }

    @Override
    public void onDisable() {
        for (PhysicalMachine machine : TekkitAPI.getMachineManager().getMachines()) {
            machine.save();
        }
        try {
            sqlite.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public TekkitMachineManager createMachineManager(Plugin plugin) {
        return new TekkitMachineManager(plugin);
    }

    @Override
    public TekkitMachineManager getMachineManager() {
        return machineManager;
    }

    private void setupConfig() {
        FileConfiguration config = getConfig();
        config.addDefault("auto-equip", true);
        config.addDefault("visible-transport", false);
        config.addDefault("max-transport-blocks", 1000);
        config.options().copyDefaults(true);
        saveConfig();

        AUTO_EQUIP = config.getBoolean("auto-equip");
        VISIBLE_TRANSPORT = config.getBoolean("visible-transport");
        MAX_TRANSPORT_BLOCKS = config.getInt("max-transport-blocks");
    }

    public Sqlite getSqlite() {
        return sqlite;
    }

    /**
     * Register all events in all the specified listener classes
     *
     * @param listeners Listeners to register
     */
    public static void registerListener(Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, TekkitAPI.getInstance());
        }
    }

    /**
     * Dynamically register a command
     *
     * @param command The command to register
     */
    public static void registerCommand(Command command) {
        CommandMap commandMap = null;
        try {
            Field field = SimplePluginManager.class.getDeclaredField("commandMap");
            field.setAccessible(true);
            commandMap = (CommandMap) field.get(Bukkit.getPluginManager());
            commandMap.register(TekkitAPI.getInstance().getName(), command);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public Machine getRegisteredMachine(String name) {
        return getMachineManager().getRegisteredMachine(name);
    }
}
