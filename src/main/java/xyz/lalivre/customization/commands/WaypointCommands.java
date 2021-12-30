package xyz.lalivre.customization.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WaypointCommands implements CommandExecutor, TabCompleter {
    private final ConcurrentHashMap<UUID, ConcurrentHashMap<String, Location>> waypoints;

    public WaypointCommands() {
        this.waypoints = new ConcurrentHashMap<>();
    }

    @NotNull
    private TextComponent waypointToComponent(Map.Entry<String, Location> entry) {
        TextComponent.Builder component = Component.empty().toBuilder();
        component.append(Component.text(entry.getKey()).color(NamedTextColor.GOLD));
        component.append(Component.text(String.format(": %d, %d, %d (%s)", entry.getValue().getBlockX(), entry.getValue().getBlockY(), entry.getValue().getBlockZ(), entry.getValue().getWorld().getEnvironment().name())));
        return component.build();
    }

    @NotNull
    private TextComponent operationList(ConcurrentHashMap<String, Location> playerWaypoints) {
        TextComponent.Builder component = Component.empty().toBuilder();
        if (playerWaypoints == null || playerWaypoints.size() == 0) {
            component.append(Component.text("Aucun waypoint à afficher.").color(NamedTextColor.RED));
        } else {
            Iterator<Map.Entry<String, Location>> iterator = new TreeMap<>(playerWaypoints).entrySet().iterator();
            while (iterator.hasNext()) {
                component.append(waypointToComponent(iterator.next()));
                if (iterator.hasNext()) {
                    component.append(Component.newline());
                }
            }
        }
        return component.build();
    }

    @NotNull
    private TextComponent operationAdd(@NotNull Player player, @NotNull String waypointName) {
        if (this.waypoints.get(player.getUniqueId()) == null) {
            player.getServer().getConsoleSender().sendMessage(Component.text("[CUSTOMIZATION]: ").color(NamedTextColor.GOLD).append(
                    Component.text(String.format("creation de la liste de waypoints de %s.", player.getName()))
            ));
            this.waypoints.put(player.getUniqueId(), new ConcurrentHashMap<>());
        }
        ConcurrentHashMap<String, Location> playerWaypoints = this.waypoints.get(player.getUniqueId());
        if (playerWaypoints.get(waypointName) != null) {
            return Component.text("Ce waypoint existe déjà.").color(NamedTextColor.RED);
        }
        if (waypointName.equals("tp") || waypointName.equals("list") || waypointName.equals("add") || waypointName.equals("remove")) {
            return Component.text("Ce nom est interdit.").color(NamedTextColor.RED);
        }
        playerWaypoints.put(waypointName, player.getLocation());
        player.getServer().getConsoleSender().sendMessage(Component.text("[CUSTOMIZATION]: ").color(NamedTextColor.GOLD).append(
                Component.text(String.format("creation du waypoint %s pour %s.", waypointName, player.getName()))
        ));
        return Component.text(waypointName).color(NamedTextColor.GOLD).append(
                Component.text(" ajouté.").color(NamedTextColor.GREEN)
        );
    }

    @NotNull
    private TextComponent operationRemove(Player player, @NotNull String waypointName) {
        ConcurrentHashMap<String, Location> playerWaypoints = this.waypoints.get(player.getUniqueId());
        if (playerWaypoints == null || !playerWaypoints.containsKey(waypointName)) {
            return Component.text("Ce waypoint n'existe pas.").color(NamedTextColor.RED);
        }
        playerWaypoints.remove(waypointName);
        player.getServer().getConsoleSender().sendMessage(Component.text("[CUSTOMIZATION]: ").color(NamedTextColor.GOLD).append(
                Component.text(String.format("suppression du waypoint %s pour %s.", waypointName, player.getName()))
        ));
        return Component.text(waypointName).color(NamedTextColor.GOLD).append(
                Component.text(" retiré.").color(NamedTextColor.GREEN)
        );
    }

    @NotNull
    private TextComponent operationSee(ConcurrentHashMap<String, Location> playerWaypoints, @NotNull String waypointName) {
        if (playerWaypoints == null || !playerWaypoints.containsKey(waypointName)) {
            return Component.text("Ce waypoint n'existe pas.").color(NamedTextColor.RED);
        }
        return waypointToComponent(new AbstractMap.SimpleEntry<>(waypointName, playerWaypoints.get(waypointName)));
    }

    private TextComponent operationTp(ConcurrentHashMap<String, Location> playerWaypoints, @NotNull Player player, @NotNull String waypointName) {
        if (!player.isOp()) {
            return Component.text("Tu n'as pas les permissions pour faire ça.").color(NamedTextColor.RED);
        }
        if (playerWaypoints == null || !playerWaypoints.containsKey(waypointName)) {
            return Component.text("Ce waypoint n'existe pas.").color(NamedTextColor.RED);
        }
        Location waypoint = playerWaypoints.get(waypointName);
        World waypointWorld = waypoint.getWorld();
        World playerWorld = player.getWorld();
        if (waypointWorld.getUID() != playerWorld.getUID()) {
            return Component.text(waypointName).color(NamedTextColor.GOLD).append(
                    Component.text(" est dans ").color(NamedTextColor.RED).append(
                            Component.text(waypointWorld.getEnvironment().name()).color(NamedTextColor.GOLD).append(
                                    Component.text(" et tu es dans ").color(NamedTextColor.RED).append(
                                            Component.text(playerWorld.getEnvironment().name()).color(NamedTextColor.GOLD).append(
                                                    Component.text(".").color(NamedTextColor.RED)
                                            )
                                    )
                            )
                    )
            );
        }
        boolean success = player.teleport(waypoint, PlayerTeleportEvent.TeleportCause.PLUGIN);
        if (success) {
            return Component.text("Téléporté avec succès à ").color(NamedTextColor.GREEN).append(
                    Component.text(waypointName).color(NamedTextColor.GOLD).append(
                            Component.text(".").color(NamedTextColor.GREEN)
                    )
            );
        }
        return Component.text("Error lors de la téléportation. Est-ce qu'un bloc obstrue la destination ?").color(NamedTextColor.RED);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Ne peut être utilisé que pas les joueurs.").color(NamedTextColor.RED));
            return false;
        }

        ConcurrentHashMap<String, Location> playerWaypoints = waypoints.get(player.getUniqueId());

        TextComponent component;

        switch (args.length) {
            case 1 -> {
                if (args[0].equals("list"))
                    component = operationList(playerWaypoints);
                else if (!args[0].equals("tp") && !args[0].equals("add") && !args[0].equals("remove")) {
                    component = (operationSee(playerWaypoints, args[0]));
                } else {
                    return false;
                }
            }
            case 2 -> {
                switch (args[0]) {
                    case "tp" -> component = (operationTp(playerWaypoints, player, args[1]));
                    case "add" -> component = (operationAdd(player, args[1]));
                    case "remove" -> component = (operationRemove(player, args[1]));
                    default -> {
                        return false;
                    }
                }
            }
            default -> {
                return false;
            }

        }
        player.sendMessage(component);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }
        ArrayList<String> completions = new ArrayList<>();
        ArrayList<String> commands = new ArrayList<>();
        commands.add("list");
        commands.add("add");
        commands.add("remove");

        if (player.isOp()) {
            commands.add("tp");
        }
        ArrayList<String> waypoints = new ArrayList<>();
        ConcurrentHashMap<String, Location> playerWaypoints = this.waypoints.get(player.getUniqueId());
        if (playerWaypoints != null) {
            playerWaypoints.forEach((name, location) -> waypoints.add(name));
        }
        switch (args.length) {
            case 1 -> {
                StringUtil.copyPartialMatches(args[0], commands, completions);
                StringUtil.copyPartialMatches(args[0], waypoints, completions);
            }
            case 2 -> {
                if (args[0].equals("tp") || args[0].equals("remove"))
                StringUtil.copyPartialMatches(args[1], waypoints, completions);
            }
        }
        Collections.sort(completions);
        return completions;
    }
}
