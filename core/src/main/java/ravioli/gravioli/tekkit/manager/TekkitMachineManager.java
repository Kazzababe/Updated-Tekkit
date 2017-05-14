package ravioli.gravioli.tekkit.manager;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import ravioli.gravioli.tekkit.Tekkit;
import ravioli.gravioli.tekkit.api.machines.Machine;
import ravioli.gravioli.tekkit.api.machines.PhysicalMachine;
import ravioli.gravioli.tekkit.api.machines.serializers.DatabaseSerializer;
import ravioli.gravioli.tekkit.api.manager.MachineManager;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.*;

public class TekkitMachineManager implements MachineManager {
    private Tekkit plugin;

    private Map<Class, DatabaseSerializer> serializers = new HashMap();
    private Map<String, Machine> registeredMachines = new HashMap();
    private Map<Location, PhysicalMachine> loadedMachines = new HashMap();
    private Map<String, Set<PhysicalMachine>> unloadedMachines = new HashMap();
    private Set<PhysicalMachine> markedForDelete = new HashSet();

    public TekkitMachineManager(Plugin plugin) {
        if (!(plugin instanceof Tekkit)) {
            throw new IllegalArgumentException("plugin must be instance of " + Tekkit.class);
        }
        this.plugin = (Tekkit) plugin;
    }

    public Machine getRegisteredMachine(String name) {
        return registeredMachines.get(name.toLowerCase());
    }

    @Override
    public void registerSerializer(Class type, DatabaseSerializer serializer) {
        serializers.put(type, serializer);
    }

    @Override
    public DatabaseSerializer getSerializer(Class type) {
        return serializers.get(type);
    }

    @Override
    public boolean hasSerializer(Class type) {
        return serializers.containsKey(type);
    }

    @Override
    public void registerMachine(Machine machine) {
        machine.register(plugin);
        registeredMachines.put(machine.getName().toLowerCase(), machine);

        if (machine instanceof PhysicalMachine) {
            plugin.getSqlite().loadMachines((PhysicalMachine) machine);
        }
    }

    @Override
    public void addMachine(PhysicalMachine machine) {
        if (loadedMachines.containsKey(machine.getLocation())) {
            throw new KeyAlreadyExistsException(machine.getName() + " at location " + machine.getLocation() + " already exists");
        }
        loadedMachines.put(machine.getLocation(), machine);
    }

    @Override
    public void removeMachine(PhysicalMachine machine) {
        markedForDelete.add(machine);
        loadedMachines.remove(machine.getLocation());
    }

    @Override
    public Collection<PhysicalMachine> getMachines() {
        return loadedMachines.values();
    }

    @Override
    public PhysicalMachine getMachine(Location location) {
        return loadedMachines.get(location);
    }

    @Override
    public PhysicalMachine getMachine(ItemStack itemStack) {
        for (Machine machine : registeredMachines.values()) {
            if (machine instanceof PhysicalMachine) {
                PhysicalMachine physicalMachine = (PhysicalMachine) machine;
                if (physicalMachine.getRecipe().getResult().isSimilar(itemStack)) {
                    return physicalMachine;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isMachine(Location location) {
        return loadedMachines.containsKey(location);
    }

    public Collection<PhysicalMachine> getMachinesMarkedForDeletion() {
        return markedForDelete;
    }

    public Collection<PhysicalMachine> getUnloadedMachines(String worldName) {
        if (unloadedMachines.containsKey(worldName)) {
            return unloadedMachines.get(worldName);
        }
        return new HashSet();
    }

    public void addUnloadedMachine(String worldName, PhysicalMachine machine) {
        if (!unloadedMachines.containsKey(worldName)) {
            unloadedMachines.put(worldName, new HashSet());
        }
        unloadedMachines.get(worldName).add(machine);
    }
}
