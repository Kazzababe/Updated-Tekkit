package ravioli.gravioli.tekkit.machines.serializers;

import org.bukkit.Location;
import ravioli.gravioli.tekkit.api.machines.serializers.DatabaseSerializer;
import ravioli.gravioli.tekkit.machines.utils.MachineUtils;

public class LocationSerializer implements DatabaseSerializer<Location> {
    @Override
    public String serialize(Location object) {
        return MachineUtils.locationToString(object);
    }

    @Override
    public Location deserialize(String object) {
        return MachineUtils.stringToLocation(object);
    }
}
