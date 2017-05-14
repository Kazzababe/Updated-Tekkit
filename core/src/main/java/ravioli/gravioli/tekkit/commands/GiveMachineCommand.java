package ravioli.gravioli.tekkit.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ravioli.gravioli.tekkit.Tekkit;
import ravioli.gravioli.tekkit.api.TekkitAPI;
import ravioli.gravioli.tekkit.api.machines.Machine;

public class GiveMachineCommand extends Command {
    private Tekkit plugin;

    public GiveMachineCommand(Tekkit plugin) {
        super("machine", "givemachine");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;

        if (args.length == 1) {
            Machine machine = plugin.getRegisteredMachine(args[0]);
            if (machine != null) {
                if (player.hasPermission("tekkitinspired.givemachine.all") || player.hasPermission("tekkitinspired.givemachine." + machine.getName().toLowerCase())) {
                    player.getInventory().addItem(machine.getRecipe().getResult());
                }
            }
        }
        return true;
    }
}
