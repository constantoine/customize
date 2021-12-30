package xyz.lalivre.customization.events;

import io.papermc.paper.event.player.PlayerDeepSleepEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

public class SleepEvents implements Listener {
    private final JavaPlugin plugin;
    private UUID overworldID;

    public SleepEvents(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        for (World world : this.plugin.getServer().getWorlds()) {
            if (world.getEnvironment() == World.Environment.NORMAL) {
                this.overworldID = world.getUID();
                break;
            }
        }
    }

    private int[] getSleepingPlayerRatio(@NotNull World world) {
        Collection<? extends Player> players = world.getPlayers();
        final int totalPlayerCount = players.size();
        final int target = (int) Math.ceil((double) totalPlayerCount / 2.0);
        int sleepingPlayerCount = 1;
        for (Player player : players) {
            if (player.isSleeping()) {
                sleepingPlayerCount++;
            }
        }
        int[] ratio = new int[2];
        ratio[0] = sleepingPlayerCount;
        ratio[1] = target;
        return ratio;
    }

    @EventHandler
    public void onPlayerBedEnter(@NotNull PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) {
            return;
        }
        World world = plugin.getServer().getWorld(overworldID);
        assert world != null;
        int[] ratio = getSleepingPlayerRatio(world);
        TextComponent.Builder component = Component.text(String.format("%d/%d ", ratio[0], ratio[1])).toBuilder();
        if (ratio[0] >= ratio[1]) {
            component.color(NamedTextColor.GREEN);
        } else {
            component.color(NamedTextColor.RED);
        }
        component.append(
                Component
                        .text("joueurs couchés.")
                        .color(NamedTextColor.YELLOW)
        );
        event.getPlayer().sendActionBar(component.build());
    }

    @EventHandler
    public void onPlayerDeepSleep(@NotNull PlayerDeepSleepEvent event) {
        World world = plugin.getServer().getWorld(overworldID);
        assert world != null;

        int[] ratio = getSleepingPlayerRatio(world);
        if (ratio[0] >= ratio[1]) {
            world.setTime(1000);
        }
    }
}
