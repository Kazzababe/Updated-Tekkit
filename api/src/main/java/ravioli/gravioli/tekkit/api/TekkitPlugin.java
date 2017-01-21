package ravioli.gravioli.tekkit.api;

import org.bukkit.plugin.Plugin;
import ravioli.gravioli.tekkit.api.manager.MachineManager;

public interface TekkitPlugin extends Plugin {
    MachineManager createMachineManager(Plugin plugin);
    MachineManager getMachineManager();
}
