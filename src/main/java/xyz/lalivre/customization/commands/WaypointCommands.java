package xyz.lalivre.customization.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lalivre.customization.types.WaypointData;

import java.util.*;

public class WaypointCommands implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;

    public WaypointCommands(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }


    @NotNull
    private TextComponent waypointToComponent(Map.Entry<String, Location> entry) {
        TextComponent.Builder component = Component.empty().toBuilder();
        component.append(Component.text(entry.getKey()).color(NamedTextColor.GOLD));
        component.append(Component.text(String.format(": %d, %d, %d (%s)", entry.getValue().getBlockX(), entry.getValue().getBlockY(), entry.getValue().getBlockZ(), entry.getValue().getWorld().getEnvironment().name())));
        return component.build();
    }

    @NotNull
    private TextComponent operationList(@NotNull Player player) {
        HashMap<String, Location> waypoints = WaypointData.getPlayerWaypoints(player, this.plugin);

        TextComponent.Builder component = Component.empty().toBuilder();
        if (waypoints.size() == 0) {
            component.append(Component.text("Aucun waypoint à afficher.").color(NamedTextColor.RED));
        } else {
            Iterator<Map.Entry<String, Location>> iterator = new TreeMap<>(waypoints).entrySet().iterator();
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
        HashMap<String, Location> playerWaypoints = WaypointData.getPlayerWaypoints(player, this.plugin);
        if (playerWaypoints.size() == 10) {
            return Component.text("Chaque joueur a une limite de 10 waypoints.").color(NamedTextColor.RED);
        }
        PersistentDataContainer container = player.getPersistentDataContainer();
        if (playerWaypoints.get(waypointName) != null) {
            return Component.text("Ce waypoint existe déjà.").color(NamedTextColor.RED);
        }
        if (waypointName.equals("tp") || waypointName.equals("list") || waypointName.equals("add") || waypointName.equals("remove")) {
            return Component.text("Ce nom est interdit.").color(NamedTextColor.RED);
        }
        playerWaypoints.put(waypointName, player.getLocation());
        try {
            container.set(new NamespacedKey(this.plugin, "waypoints/" + waypointName), new WaypointData(this.plugin), player.getLocation());
        } catch (Exception e) {
            return Component.text(String.format("Nom de waypoint invalide: %s", e.getMessage())).color(NamedTextColor.RED);
        }

        player.getServer().getConsoleSender().sendMessage(Component.text("[CUSTOMIZATION]: ").color(NamedTextColor.GOLD).append(
                Component.text(String.format("creation du waypoint %s pour %s.", waypointName, player.getName()))
        ));
        return Component.text(waypointName).color(NamedTextColor.GOLD).append(
                Component.text(" ajouté.").color(NamedTextColor.GREEN)
        );
    }

    @NotNull
    private TextComponent operationRemove(@NotNull Player player, @NotNull String waypointName) {
        HashMap<String, Location> playerWaypoints = WaypointData.getPlayerWaypoints(player, this.plugin);
        PersistentDataContainer container = player.getPersistentDataContainer();
        if (!playerWaypoints.containsKey(waypointName)) {
            return Component.text("Ce waypoint n'existe pas.").color(NamedTextColor.RED);
        }
        try {
            container.remove(new NamespacedKey(this.plugin, "waypoints/" + waypointName));
        } catch (Exception e) {
            return Component.text(String.format("Nom de waypoint invalide: %s", e.getMessage())).color(NamedTextColor.RED);
        }
        player.getServer().getConsoleSender().sendMessage(Component.text("[CUSTOMIZATION]: ").color(NamedTextColor.GOLD).append(
                Component.text(String.format("suppression du waypoint %s pour %s.", waypointName, player.getName()))
        ));
        return Component.text(waypointName).color(NamedTextColor.GOLD).append(
                Component.text(" retiré.").color(NamedTextColor.GREEN)
        );
    }

    @NotNull
    private TextComponent operationSee(@NotNull Player player, @NotNull String waypointName) {
        HashMap<String, Location> playerWaypoints = WaypointData.getPlayerWaypoints(player, this.plugin);
        if (!playerWaypoints.containsKey(waypointName)) {
            return Component.text("Ce waypoint n'existe pas.").color(NamedTextColor.RED);
        }
        return waypointToComponent(new AbstractMap.SimpleEntry<>(waypointName, playerWaypoints.get(waypointName)));
    }

    private TextComponent operationTp(@NotNull Player player, @NotNull String waypointName) {
        HashMap<String, Location> playerWaypoints = WaypointData.getPlayerWaypoints(player, this.plugin);
        if (!playerWaypoints.containsKey(waypointName)) {
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

        TextComponent component;

        switch (args.length) {
            case 1 -> {
                if (args[0].equals("list"))
                    component = operationList(player);
                else if (!args[0].equals("tp") && !args[0].equals("add") && !args[0].equals("remove")) {
                    component = (operationSee(player, args[0]));
                } else {
                    return false;
                }
            }
            case 2 -> {
                switch (args[0]) {
                    case "tp" -> component = (operationTp(player, args[1]));
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
        Set<String> waypoints = new HashSet<>();
        HashMap<String, Location> playerWaypoints = WaypointData.getPlayerWaypoints(player, this.plugin);
        playerWaypoints.forEach((name, location) -> waypoints.add(name));
        switch (args.length) {
            case 1 -> {
                StringUtil.copyPartialMatches(args[0], commands, completions);
                StringUtil.copyPartialMatches(args[0], waypoints, completions);
            }
            case 2 -> {
                if (args[0].equals("tp") || args[0].equals("remove")) {
                    StringUtil.copyPartialMatches(args[1], waypoints, completions);
                }
            }
        }
        Collections.sort(completions);
        return completions;
    }
}
