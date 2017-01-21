package ravioli.gravioli.tekkit.api.manager;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import ravioli.gravioli.tekkit.api.machines.PhysicalMachine;
import ravioli.gravioli.tekkit.api.machines.Machine;
import ravioli.gravioli.tekkit.api.machines.serializers.DatabaseSerializer;

import java.util.Collection;

public interface MachineManager {
    /**
     * Attach a serializer to a class type
     *
     * @param type the class to attach the serializer to
     * @param serializer the serializer to attach
     */
    void registerSerializer(Class type, DatabaseSerializer serializer);

    /**
     * Get the serializer attached to the specified class type
     *
     * @param type the class type to check
     * @return the serializer for the specified class type
     */
    DatabaseSerializer getSerializer(Class type);

    /**
     * Check if the specified class type has a serializer attached to it
     *
     * @param type class type to check
     * @return if the class type has a serializer
     */
    boolean hasSerializer(Class type);

    void registerMachine(Machine machine);
    void addMachine(PhysicalMachine machine);
    void removeMachine(PhysicalMachine machine);
    Collection<PhysicalMachine> getMachines();
    PhysicalMachine getMachine(Location location);
    PhysicalMachine getMachine(ItemStack itemStack);
    boolean isMachine(Location location);
}
