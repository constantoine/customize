package xyz.lalivre.customization.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DeathCommandsCompletions implements TabCompleter {
    private final JavaPlugin plugin;

    public DeathCommandsCompletions(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        // You can only get position for one (1) player
        if (args.length > 1) {
            return Collections.emptyList();
        }
        List<String> completions = new ArrayList<>();
        // If this is a player, can only autocomplete on other player's names if the player is OP
        if (sender instanceof Player) {
            if (!sender.isOp()) {
                completions.add(sender.getName());
                return completions;
            }
        }
        Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();
        ArrayList<String> names = new ArrayList<>(players.size());

        for (Player player: players) {
            names.add(player.getName());
        }
        StringUtil.copyPartialMatches(args[0], names, completions);
        Collections.sort(completions);
        return completions;
    }
}
