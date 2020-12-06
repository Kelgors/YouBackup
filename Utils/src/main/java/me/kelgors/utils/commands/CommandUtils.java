package me.kelgors.utils.commands;

import me.kelgors.utils.chat.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class CommandUtils {

    public static String NO_PERMISSIONS = ChatUtils.colorized(ChatColor.RED, "You do not have permission to perform this command.");

    public static boolean isBlank(String arg) {
        if (arg == null)
            return true;
        if (arg.trim().length() == 0)
            return true;
        return false;
    }

    public static boolean hasArgument(String[] args, int minArgs) {
        if (args == null)
            return false;
        if (args.length < minArgs)
            return false;
        return true;
    }

    public static boolean hasAnyPermission(Player player, String[] permissions) {
        for (String permission : permissions) {
            if (player.hasPermission(permission)) return true;
        }
        return false;
    }

}