package me.kelgors.utils.chat;

import org.bukkit.ChatColor;

public class ChatUtils {
    public static String colorized(ChatColor color, String message) {
        return color + message + ChatColor.RESET;
    }
}
