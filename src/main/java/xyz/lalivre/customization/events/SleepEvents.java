package xyz.lalivre.customization.events;

import io.papermc.paper.event.player.PlayerDeepSleepEvent;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

public class SleepEvents implements Listener {
    private final JavaPlugin plugin;
    private UUID overworldID;

    public SleepEvents(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        for (World world: this.plugin.getServer().getWorlds()) {
            if (world.getEnvironment() == World.Environment.NORMAL){
                this.overworldID = world.getUID();
                break;
            }
        }
    }

    @EventHandler
    public void onPlayerDeepSleep(@NotNull PlayerDeepSleepEvent event) {
        World world = plugin.getServer().getWorld(overworldID);
        assert world != null;

        Collection<? extends Player> players = world.getPlayers();
        final int totalPlayerCount = players.size();
        final int target = (int) Math.ceil((double) totalPlayerCount / 2.0);
        int sleepingPlayerCount = 1;
        for (Player player: players) {
            if (player.isSleeping()) {
                sleepingPlayerCount++;
            }
        }
        if (sleepingPlayerCount >= target) {
            world.setTime(1000);
            return;
        }
        event.getPlayer().sendMessage(String.format("%d/%d sleeping", sleepingPlayerCount, totalPlayerCount));
    }
}
