package xyz.lalivre.customization.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DeathEvents implements Listener {
    private final JavaPlugin plugin;
    public static ConcurrentHashMap<UUID, Location> deaths = new ConcurrentHashMap<>();

    public DeathEvents(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public ConcurrentHashMap<UUID, Location> getDeaths() {
        return deaths;
    }

    @EventHandler
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        Player player = event.getPlayer();
        deaths.put(player.getUniqueId(), player.getLocation());

        EntityDamageEvent cause = event.getEntity().getLastDamageCause();
        Sound sound = Sound.ENTITY_WITHER_SPAWN;
        String causeText = " nous a quittés.";

        if (cause != null) {
            switch (cause.getCause()) {
                case ENTITY_EXPLOSION, BLOCK_EXPLOSION -> {
                    sound = Sound.ENTITY_GENERIC_EXPLODE;
                    causeText = " s'est transformé⋅e en plein de petits morceaux.";
                }
                case POISON, MAGIC -> {
                    sound = Sound.ENTITY_WITCH_DRINK;
                    causeText = " a accepté un bonbon de la part d'un inconnu.";
                }
                case FALLING_BLOCK ->  {
                    sound = Sound.BLOCK_ANVIL_LAND;
                    causeText = " s'est fait⋅e applatir.";
                }
                case FALL -> {
                    sound = Sound.ENTITY_PLAYER_BIG_FALL;
                    causeText = " a découvert la gravité.";
                }
                case FLY_INTO_WALL -> {
                    sound = Sound.ENTITY_PLAYER_BIG_FALL;
                    causeText = " a imité le 11 septembre.";
                }
                case MELTING, FIRE, FIRE_TICK -> {
                    sound = Sound.BLOCK_LAVA_EXTINGUISH;
                    causeText = " a voulu faire un BBQ.";
                }
                case VOID -> {
                    sound = Sound.BLOCK_ANVIL_LAND;
                    causeText = " a voulu défier les admins.";
                }
            }
        }

        final TextComponent textComponent = Component
                .text(player.getName())
                .color(NamedTextColor.YELLOW)
                .append(
                        Component.text(causeText)
                                .color(NamedTextColor.RED)
                );

        event.deathMessage(textComponent);

        Collection<? extends Player> players = this.plugin.getServer().getOnlinePlayers();

        for (Player p: players) {
            p.sendActionBar(textComponent);
            p.playSound(p.getLocation(), sound, 0.8f, 1.0f);
        }
    }
}
