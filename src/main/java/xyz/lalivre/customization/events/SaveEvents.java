package xyz.lalivre.customization.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;

public class SaveEvents implements Listener {
    @EventHandler
    public void onWorldSave(WorldSaveEvent event) {
        TextComponent subTitle = Component.text("Risque de lags pendant quelques instants").color(NamedTextColor.YELLOW);
        TextComponent title = Component.text("Sauvegarde du monde.").color(NamedTextColor.GREEN).append(Component.newline().append(subTitle));
        for (Player player: event.getWorld().getPlayers()) {
            player.sendMessage(title);
        }
    }
}
