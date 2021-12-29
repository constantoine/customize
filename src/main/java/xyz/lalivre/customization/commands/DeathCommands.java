package xyz.lalivre.customization.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DeathCommands implements CommandExecutor {
    private final JavaPlugin plugin;
    public ConcurrentHashMap<UUID, Location> deaths;

    public DeathCommands(JavaPlugin plugin, ConcurrentHashMap<UUID, Location> deaths) {
        this.plugin = plugin;
        this.deaths = deaths;
    }

    private void sendLastDeathCoordinates(@NotNull CommandSender sender, @NotNull Player player) {
        final Location pos = this.deaths.get(player.getUniqueId());

        // If self requested
        if ((sender instanceof Player) && ((Player) sender).getUniqueId() == player.getUniqueId()) {
            if (pos == null) {
                sender.sendMessage("You haven't died yet!");
                return;
            }
            sender.sendMessage(String.format("You died at: %d, %d, %d", pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()));
            return;
        }
        if (pos == null) {
            sender.sendMessage(String.format("%s hasn't died yet!", player.getName()));
            return;
        }
        sender.sendMessage(String.format("%s died at: %d, %d, %d", player.getName(), pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        switch (args.length) {
            case 0 -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "You need to specify one player.");
                    return false;
                }
                sendLastDeathCoordinates(sender, (Player) sender);
            }
            case 1 -> {
                final Player player = plugin.getServer().getPlayerExact(args[0]);
                if (player == null) {
                    sender.sendMessage(ChatColor.RED + "Invalid player name.");
                    return false;
                }
                // If requested by another player, check that they're OP
                if ((sender instanceof Player) && ((Player) sender).getUniqueId() != player.getUniqueId() && !player.isOp()) {
                    sender.sendMessage(ChatColor.RED + "You don't have permissions to request someone else's death point.");
                    return false;
                }
                sendLastDeathCoordinates(sender, player);
            }
            default -> {
                sender.sendMessage(ChatColor.RED + "Only one player may be specified.");
                return false;
            }
        }
        return true;
    }
}
