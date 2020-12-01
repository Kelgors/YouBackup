package me.kelgors.ubackup.commands.ubackup;

import me.kelgors.ubackup.WorldConfiguration;
import me.kelgors.ubackup.uBackupPlugin;
import me.kelgors.utils.chat.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EnableSubCommand extends AbsWorldRelatedSubCommand {
    @Override
    public boolean checkPermission(Player player) {
        return player.hasPermission("me.kelgors.me.kelgors.me.kelgors.ubackup.enable") || player.hasPermission("me.kelgors.me.kelgors.me.kelgors.ubackup.*");
    }

    @Override
    public boolean isOnlyPlayerSubCommand() {
        return false;
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String commandName, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(uBackupPlugin.TAG + "Please specify a world name. /me.kelgors.me.kelgors.me.kelgors.ubackup enable <world_name>");
            return true;
        }
        final String worldName = args[0];
        final WorldConfiguration config = ((uBackupPlugin) mPlugin).getWorldConfiguration(worldName);
        final World world = mPlugin.getServer().getWorld(worldName);
        if (config == null) {
            sender.sendMessage(uBackupPlugin.TAG + "Unknown world " + ChatUtils.colorized(ChatColor.BLUE, worldName) + " in uBackup config.yml");
            return true;
        }
        if (world == null) {
            sender.sendMessage(uBackupPlugin.TAG + "World " + ChatUtils.colorized(ChatColor.BLUE, worldName) + " does not exists on your server");
            return true;
        }
        mPlugin.getConfig().set(String.format("worlds.%s.enabled", worldName), true);
        config.enabled = true;
        sender.sendMessage(String.format("The world %s has been enabled", ChatUtils.colorized(ChatColor.BLUE, worldName)));
        return true;
    }

    @Override
    public boolean execute(Player player, Command command, String commandName, String[] args) {
        return execute((CommandSender) player, command, commandName, args);
    }
}
