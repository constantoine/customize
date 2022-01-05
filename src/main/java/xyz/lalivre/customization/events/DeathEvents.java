package xyz.lalivre.customization.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import xyz.lalivre.customization.types.WaypointData;

import java.util.Collection;

public class DeathEvents implements Listener {
    private final JavaPlugin plugin;

    public DeathEvents(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public static NamespacedKey deathKey(@NotNull JavaPlugin plugin) {
        return new NamespacedKey(plugin, "death");
    }

    @EventHandler
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        Player player = event.getPlayer();
        PersistentDataContainer container = player.getPersistentDataContainer();
        container.set(deathKey(this.plugin), new WaypointData(this.plugin), player.getLocation());

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
                case FALLING_BLOCK -> {
                    sound = Sound.BLOCK_ANVIL_LAND;
                    causeText = " s'est fait⋅e applatir.";
                }
                case FALL -> {
                    sound = Sound.BLOCK_ANVIL_LAND;
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

        Component message = event.deathMessage();
        if (message != null) {
            this.plugin.getServer().getConsoleSender().sendMessage(message);
        }

        event.deathMessage(textComponent);

        Collection<? extends Player> players = this.plugin.getServer().getOnlinePlayers();

        for (Player p : players) {
            p.sendActionBar(textComponent);
            p.playSound(p.getLocation(), sound, 0.8f, 1.0f);
        }
    }
}
