package xyz.lalivre.customization.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DeathEvents implements Listener {
    public static ConcurrentHashMap<UUID, Location> deaths = new ConcurrentHashMap<>();

    public ConcurrentHashMap<UUID, Location> getDeaths() {
        return deaths;
    }

    @EventHandler
    public static void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        Player player = event.getPlayer();
        Location pos = player.getLocation();
        deaths.put(player.getUniqueId(), pos);

        player.sendMessage(String.format("You died at: %d, %d, %d", pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()));
    }
}
