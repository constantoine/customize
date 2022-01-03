package xyz.lalivre.customization.types;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.UUID;

public class WaypointData implements PersistentDataType<byte [], Location> {
    private final JavaPlugin plugin;

    public WaypointData(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public static HashMap<String, Location> getPlayerWaypoints(@NotNull Player player, @NotNull JavaPlugin plugin) {
        final NamespacedKey namespacedKey = new NamespacedKey(plugin, "waypoints/");
        PersistentDataContainer container = player.getPersistentDataContainer();
        HashMap<String, Location> waypoints = new HashMap<>();
        for (NamespacedKey key : container.getKeys()) {
            if (!key.namespace().equals(namespacedKey.namespace())) {
                continue;
            }
            String keyTitle;
            if (key.getKey().startsWith("waypoints/")) {
                keyTitle = key.getKey().substring("waypoints/".length());
            } else {
                continue;
            }
            Location loc;
            try {
                loc = container.get(key, new WaypointData(plugin));
            } catch (IllegalArgumentException e) {
                container.remove(key);
                continue;
            }
            waypoints.put(keyTitle, loc);
        }
        return waypoints;
    }

    @Override
    public @NotNull Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public @NotNull Class<Location> getComplexType() {
        return Location.class;
    }

    @Override
    public byte @NotNull [] toPrimitive(@NotNull Location complex, @NotNull PersistentDataAdapterContext context) {
        ByteBuffer result = ByteBuffer.wrap(new byte[16 + Integer.BYTES * 3]);
        UUID uuid = complex.getWorld().getUID();
        result.putLong(uuid.getMostSignificantBits());
        result.putLong(uuid.getLeastSignificantBits());
        result.putInt(complex.getBlockX());
        result.putInt(complex.getBlockY());
        result.putInt(complex.getBlockZ());
        return result.array();
    }

    @Override
    public @NotNull Location fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) throws IllegalArgumentException {
        ByteBuffer input = ByteBuffer.wrap(primitive);
        UUID uuid = new UUID(input.getLong(), input.getLong());
        World world = this.plugin.getServer().getWorld(uuid);
        if (world == null) {
            throw new IllegalArgumentException();
        }
        int x = input.getInt();
        int y = input.getInt();
        int z = input.getInt();
        return new Location(world, x, y, z);
    }
}
