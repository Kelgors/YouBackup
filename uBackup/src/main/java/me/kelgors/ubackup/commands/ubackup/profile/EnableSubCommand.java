package me.kelgors.ubackup.commands.ubackup.profile;

import me.kelgors.ubackup.YouBackupPlugin;
import me.kelgors.ubackup.commands.ubackup.AbsWorldRelatedSubCommand;
import me.kelgors.ubackup.configuration.BackupConfiguration;
import me.kelgors.utils.chat.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EnableSubCommand extends AbsWorldRelatedSubCommand {
    private final String mProfileName;

    public EnableSubCommand(String profileName) {
        mProfileName = profileName;
    }

    @Override
    public boolean checkPermission(Player player) {
        return player.hasPermission("youbackup.enable") || player.hasPermission("youbackup.*");
    }

    @Override
    public boolean isOnlyPlayerSubCommand() {
        return false;
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String commandName, String[] args) {
        final BackupConfiguration config = ((YouBackupPlugin) mPlugin).getProfileConfiguration(mProfileName);
        if (config == null) {
            sender.sendMessage(YouBackupPlugin.TAG + "Unknown profile " + ChatUtils.colorized(ChatColor.BLUE, mProfileName) + " in YouBackup config.yml");
            return true;
        }
        mPlugin.getConfig().set(String.format("backups.%s.enabled", mProfileName), true);
        mPlugin.saveConfig();
        config.setEnabled(true);
        sender.sendMessage(String.format("The world %s has been enabled", ChatUtils.colorized(ChatColor.BLUE, mProfileName)));
        return true;
    }

    @Override
    public boolean execute(Player player, Command command, String commandName, String[] args) {
        return execute((CommandSender) player, command, commandName, args);
    }
}
