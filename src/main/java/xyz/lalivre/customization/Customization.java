package xyz.lalivre.customization;

import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.lalivre.customization.commands.DeathCommands;
import xyz.lalivre.customization.events.DeathEvents;

import java.util.Objects;

public final class Customization extends JavaPlugin {

    @Override
    public void onEnable() {
        Server server = getServer();
        DeathEvents deathEvents = new DeathEvents();
        DeathCommands deathCommands = new DeathCommands(deathEvents.getDeaths());

        server.getPluginManager().registerEvents(deathEvents, this);
        Objects.requireNonNull(getCommand("death")).setExecutor(deathCommands);
        server.getConsoleSender().sendMessage("[Customization] Démarrage du plugin");

    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage("[Customization] Extinction du plugin");
    }
}
