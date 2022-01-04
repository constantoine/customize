package xyz.lalivre.customization.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.StructureType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerInteractEvents implements Listener {
    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (event.getMaterial() != Material.ENDER_EYE) {
            return;
        }
        Player player = event.getPlayer();
        Location target = player.getWorld().locateNearestStructure(player.getLocation(), StructureType.STRONGHOLD, 750, false);
        if (target == null) {
            return;
        }
        player.sendActionBar(Component.text(String.format("La forteresse est à %dm.", (int) player.getLocation().distance(target))).color(NamedTextColor.GREEN));
    }
}
