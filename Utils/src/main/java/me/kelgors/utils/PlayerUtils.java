package me.kelgors.utils;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

public class PlayerUtils {

    public static OfflinePlayer getOfflinePlayerByName(Plugin plugin, String name) {
        for (OfflinePlayer offlinePlayer : plugin.getServer().getOfflinePlayers()) {
            if (name.equals(offlinePlayer.getPlayer().getDisplayName())) return offlinePlayer;
        }
        return null;
    }
}
