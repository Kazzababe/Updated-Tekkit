package ravioli.gravioli.tekkit.machines.transport;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

public class GoldPipe extends Pipe {
    @Override
    public Recipe getRecipe() {
        ItemStack item = new ItemStack(Material.GLASS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.YELLOW + "Gold Pipe");
        item.setItemMeta(itemMeta);

        ShapedRecipe recipe = new ShapedRecipe(item);
        recipe.shape("IGI");
        recipe.setIngredient('I', Material.GOLD_INGOT);
        recipe.setIngredient('G', Material.GLASS);

        return recipe;
    }

    @Override
    public String getName() {
        return "gold_pipe";
    }

    @Override
    public String getFormattedName() {
        return "GoldPipe";
    }

    @Override
    public double getSpeed() {
        return 0.15;
    }

    @Override
    public int getTickRate() {
        return 2;
    }
}
