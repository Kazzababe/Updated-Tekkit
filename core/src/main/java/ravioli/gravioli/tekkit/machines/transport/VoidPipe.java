package ravioli.gravioli.tekkit.machines.transport;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

public class VoidPipe extends Pipe {
    @Override
    public Recipe getRecipe() {
        ItemStack item = new ItemStack(Material.GLASS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.YELLOW + "Void Pipe");
        item.setItemMeta(itemMeta);

        ShapedRecipe recipe = new ShapedRecipe(item);
        recipe.shape("EGE");
        recipe.setIngredient('E', Material.ENDER_PEARL);
        recipe.setIngredient('G', Material.GLASS);

        return recipe;
    }

    @Override
    public String getName() {
        return "void_pipe";
    }

    @Override
    public String getFormattedName() {
        return "VoidPipe";
    }

    @Override
    public double getSpeed() {
        return 0.1;
    }

    @Override
    public int getTickRate() {
        return 1;
    }

    @Override
    public void addTransportItem(TransportItem item, BlockFace input) {
        item.destroy();
    }

    public void addItem(ItemStack item, BlockFace input) {
        item = null;
    }
}
