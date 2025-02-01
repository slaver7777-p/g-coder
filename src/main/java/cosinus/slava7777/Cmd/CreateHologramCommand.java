package cosinus.slava7777.Cmd;

import cosinus.slava7777.CosHolograms;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CreateHologramCommand implements CommandExecutor, TabCompleter {

    private final CosHolograms plugin;

    public CreateHologramCommand(CosHolograms plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player player) {
            if (args.length < 1) {
                player.sendMessage("Usage: /createhologram <name>");
                return false;
            }

            String name = args[0];
            plugin.getHologramManager().createHologram(name, player.getLocation());
            player.sendMessage("Hologram '" + name + "' created! You can now configure it in the config file.");
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 && args[0].isEmpty()) {
            return Collections.singletonList("<название>");
        }
        return Collections.emptyList();
    }
}
