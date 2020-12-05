package me.kelgors.youbackup.commands.youbackup.profile;

import me.kelgors.utils.chat.ChatUtils;
import me.kelgors.youbackup.YouBackupPlugin;
import me.kelgors.youbackup.configuration.BackupConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DisableSubCommand extends AbsProfileRelatedSubCommand {
    private final String mProfileName;

    public DisableSubCommand(String profileName) {
        mProfileName = profileName;
    }

    @Override
    public boolean checkPermission(Player player) {
        return player.hasPermission("youbackup.disable") || player.hasPermission("youbackup.*");
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
        mPlugin.getConfig().set(String.format("backups.%s.enabled", mProfileName), false);
        mPlugin.saveConfig();
        config.setEnabled(false);
        sender.sendMessage(String.format("The world %s has been disabled", ChatUtils.colorized(ChatColor.BLUE, mProfileName)));
        return true;
    }

    @Override
    public boolean execute(Player player, Command command, String commandName, String[] args) {
        return execute((CommandSender) player, command, commandName, args);
    }
}
