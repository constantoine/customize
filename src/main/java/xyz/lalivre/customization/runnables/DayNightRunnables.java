package xyz.lalivre.customization.runnables;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

public class DayNightRunnables implements Runnable {
    private final JavaPlugin plugin;
    private boolean isDay;
    private UUID overworldID;

    public DayNightRunnables(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        for (World world : this.plugin.getServer().getWorlds()) {
            if (world.getEnvironment() == World.Environment.NORMAL) {
                this.overworldID = world.getUID();
                break;
            }
        }
    }

    @Override
    public void run() {
        Server server = plugin.getServer();
        World world = server.getWorld(overworldID);
        assert world != null;
        if (world.isDayTime() == this.isDay) {
            return;
        }
        Sound sound;

        this.isDay = world.isDayTime();
        Collection<? extends Player> players = world.getPlayers();
        TextComponent textComponent;
        if (this.isDay) {
            sound = Sound.ENTITY_PLAYER_LEVELUP;
            textComponent = Component
                    .text("Le jour se lève.")
                    .color(NamedTextColor.GREEN);
        } else {
            sound = Sound.ENTITY_WITHER_SPAWN;
            textComponent = Component
                    .text("La nuit tombe.")
                    .color(NamedTextColor.RED);
        }
        for (Player player : players) {
            player.sendActionBar(textComponent);
            player.playSound(player.getLocation(), sound, 0.3f, 1.0f);
        }
    }
}
