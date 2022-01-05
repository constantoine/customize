package xyz.lalivre.customization.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import xyz.lalivre.customization.events.DeathEvents;
import xyz.lalivre.customization.types.WaypointData;

import java.util.*;

public class DeathCommands implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;

    public DeathCommands(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
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

        for (Player player : players) {
            names.add(player.getName());
        }
        StringUtil.copyPartialMatches(args[0], names, completions);
        Collections.sort(completions);
        return completions;
    }

    private void sendLastDeathCoordinates(@NotNull CommandSender sender, @NotNull Player player) {
        final Location pos = player.getPersistentDataContainer().get(DeathEvents.deathKey(this.plugin), new WaypointData(this.plugin));

        // If self requested
        if ((sender instanceof Player) && ((Player) sender).getUniqueId() == player.getUniqueId()) {
            if (pos == null) {
                sender.sendMessage("Tu n'es pas encore mort⋅e !");
                return;
            }
            sender.sendMessage(String.format("Ta dernière mort était à %d, %d, %d", pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()));
            return;
        }
        if (pos == null) {
            sender.sendMessage(String.format("%s n'est pas encore mort⋅e !", player.getName()));
            return;
        }
        sender.sendMessage(String.format("La dernière mort de %s était à %d, %d, %d", player.getName(), pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        switch (args.length) {
            case 0 -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Tu dois spécifier un pseudo.");
                    return false;
                }
                sendLastDeathCoordinates(sender, (Player) sender);
            }
            case 1 -> {
                final Player player = plugin.getServer().getPlayerExact(args[0]);
                if (player == null) {
                    sender.sendMessage(ChatColor.RED + "Pseudo invalide.");
                    return false;
                }
                // If requested by another player, check that they're OP
                if ((sender instanceof Player) && ((Player) sender).getUniqueId() != player.getUniqueId() && !player.isOp()) {
                    sender.sendMessage(ChatColor.RED + "Tu t'as pas la permission pour voir la mort des autres.");
                    return false;
                }
                sendLastDeathCoordinates(sender, player);
            }
            default -> {
                sender.sendMessage(ChatColor.RED + "Tu ne peux regarder qu'une mort à la fois.");
                return false;
            }
        }
        return true;
    }
}
