package ravioli.gravioli.tekkit.machines.transport;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

public class DiamondPipe extends Pipe {
    @Override
    public Recipe getRecipe() {
        ItemStack item = new ItemStack(Material.GLASS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.YELLOW + "Diamond Pipe");
        item.setItemMeta(itemMeta);

        ShapedRecipe recipe = new ShapedRecipe(item);
        recipe.shape("DGD");
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('G', Material.GLASS);

        return recipe;
    }

    @Override
    public String getName() {
        return "diamond_pipe";
    }

    @Override
    public String getFormattedName() {
        return "DiamondPipe";
    }

    @Override
    public double getSpeed() {
        return 0.15;
    }

    @Override
    public int getTickRate() {
        return 1;
    }
}
