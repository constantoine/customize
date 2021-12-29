package xyz.lalivre.customization;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.lalivre.customization.commands.DeathCommands;
import xyz.lalivre.customization.commands.DeathCommandsCompletions;
import xyz.lalivre.customization.events.DeathEvents;
import xyz.lalivre.customization.events.SleepEvents;
import xyz.lalivre.customization.runnables.DayNightRunnables;

import java.util.Objects;

public final class Customization extends JavaPlugin {
    @Override
    public void onEnable() {
        Server server = getServer();

        DeathEvents deathEvents = new DeathEvents(this);
        SleepEvents sleepEvents = new SleepEvents(this);
        DeathCommandsCompletions deathCommandsCompletions = new DeathCommandsCompletions(this);
        DeathCommands deathCommands = new DeathCommands(this, deathEvents.getDeaths());

        server.getPluginManager().registerEvents(deathEvents, this);
        server.getPluginManager().registerEvents(sleepEvents, this);
        Objects.requireNonNull(getCommand("death")).setExecutor(deathCommands);
        Objects.requireNonNull(getCommand("death")).setTabCompleter(deathCommandsCompletions);

        server.getConsoleSender().sendMessage(
                Component
                        .text("[CUSTOMIZATION]: ")
                        .color(NamedTextColor.YELLOW).append(
                                Component
                                        .text("Démarrage du plugin.")
                                        .color(NamedTextColor.GREEN)
                        )
        );
        Bukkit.getScheduler().runTaskTimer(this, new DayNightRunnables(this), 0L, 100L);
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(
                Component
                        .text("[CUSTOMIZATION]: ")
                        .color(NamedTextColor.YELLOW).append(
                                Component
                                        .text("Extinction du plugin.")
                                        .color(NamedTextColor.RED)
                        )
        );
    }
}
