package me.kelgors.ubackup.commands.ubackup;

import me.kelgors.ubackup.configuration.BackupConfiguration;
import me.kelgors.ubackup.uBackupPlugin;
import me.kelgors.utils.chat.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DisableSubCommand extends AbsWorldRelatedSubCommand {
    @Override
    public boolean checkPermission(Player player) {
        return player.hasPermission("ubackup.disable") || player.hasPermission("ubackup.*");
    }

    @Override
    public boolean isOnlyPlayerSubCommand() {
        return false;
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String commandName, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(uBackupPlugin.TAG + "Please specify a world name. /me.kelgors.me.kelgors.me.kelgors.ubackup disable <world_name>");
            return true;
        }
        final String profileName = args[0];
        final BackupConfiguration config = ((uBackupPlugin) mPlugin).getProfileConfiguration(profileName);
        if (config == null) {
            sender.sendMessage(uBackupPlugin.TAG + "Unknown profile " + ChatUtils.colorized(ChatColor.BLUE, profileName) + " in uBackup config.yml");
            return true;
        }
        mPlugin.getConfig().set(String.format("backups.%s.enabled", profileName), false);
        mPlugin.saveConfig();
        config.setEnabled(false);
        sender.sendMessage(String.format("The world %s has been disabled", ChatUtils.colorized(ChatColor.BLUE, profileName)));
        return true;
    }

    @Override
    public boolean execute(Player player, Command command, String commandName, String[] args) {
        return execute((CommandSender) player, command, commandName, args);
    }
}
