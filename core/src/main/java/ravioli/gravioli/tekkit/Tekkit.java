package ravioli.gravioli.tekkit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandMap;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ravioli.gravioli.tekkit.api.TekkitAPI;
import ravioli.gravioli.tekkit.api.TekkitPlugin;
import ravioli.gravioli.tekkit.commands.Command;
import ravioli.gravioli.tekkit.database.Sqlite;
import ravioli.gravioli.tekkit.machines.MachineBlockBreaker;
import ravioli.gravioli.tekkit.listeners.MachineListeners;
import ravioli.gravioli.tekkit.machines.serializers.LocationSerializer;
import ravioli.gravioli.tekkit.machines.serializers.UUIDSerializer;
import ravioli.gravioli.tekkit.manager.TekkitMachineManager;

import java.lang.reflect.Field;
import java.util.UUID;

public class Tekkit extends JavaPlugin implements TekkitPlugin {
    private TekkitMachineManager machineManager;
    private Sqlite sqlite;

    @Override
    public void onLoad() {
        sqlite = new Sqlite(this);
    }

    @Override
    public void onEnable() {
        TekkitAPI.setInstance(this);
        machineManager = (TekkitMachineManager) TekkitAPI.createMachineManager(this);

        machineManager.registerSerializer(UUID.class, new UUIDSerializer());
        machineManager.registerSerializer(Location.class, new LocationSerializer());

        machineManager.registerMachine(new MachineBlockBreaker());

        registerListener(new MachineListeners(this));
    }

    @Override
    public void onDisable() {

    }

    @Override
    public TekkitMachineManager createMachineManager(Plugin plugin) {
        return new TekkitMachineManager(plugin);
    }

    @Override
    public TekkitMachineManager getMachineManager() {
        return machineManager;
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
}
