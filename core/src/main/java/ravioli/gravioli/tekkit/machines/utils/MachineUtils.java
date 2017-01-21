package ravioli.gravioli.tekkit.machines.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

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
}
