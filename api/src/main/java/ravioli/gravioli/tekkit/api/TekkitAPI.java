package ravioli.gravioli.tekkit.api;

import org.bukkit.plugin.Plugin;
import ravioli.gravioli.tekkit.api.manager.MachineManager;

public final class TekkitAPI {
    private static TekkitPlugin instance;

    public static void setInstance(TekkitPlugin plugin) {
        instance = plugin;
    }

    public static TekkitPlugin getInstance() {
        return instance;
    }

    public static MachineManager createMachineManager(Plugin plugin) {
        if (instance == null) {
            throw new IllegalStateException("API instance has not been set");
        }
        return instance.createMachineManager(plugin);
    }

    public static MachineManager getMachineManager() {
        return instance.getMachineManager();
    }
}
