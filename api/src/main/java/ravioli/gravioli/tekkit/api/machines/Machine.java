package ravioli.gravioli.tekkit.api.machines;

import org.bukkit.inventory.Recipe;
import ravioli.gravioli.tekkit.api.TekkitPlugin;

public interface Machine {
    Recipe getRecipe();

    String getName();

    String getFormattedName();

     void register(TekkitPlugin plugin);
}
