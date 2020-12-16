package me.kelgors.youbackup.commands.youbackup.profile.name;

import me.kelgors.utils.chat.ChatUtils;
import me.kelgors.utils.commands.CommandUtils;
import me.kelgors.youbackup.YouBackupPlugin;
import me.kelgors.youbackup.commands.youbackup.AbsYouBackupSubCommand;
import me.kelgors.youbackup.configuration.BackupProfile;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /yb profile &lt;profile&gt; disable
 */
public class DisableSubCommand extends AbsYouBackupSubCommand {
    private final String mProfileName;

    public DisableSubCommand(String profileName) {
        mProfileName = profileName;
    }

    @Override
    public boolean checkPermission(Player player) {
        return CommandUtils.hasAnyPermission(player, new String[] {
                String.format("youbackup.profile.%s.disable", mProfileName),
                String.format("youbackup.profile.%s.*", mProfileName),
                "youbackup.profile.*",
                "youbackup.*"
        });
    }

    @Override
    public boolean isOnlyPlayerSubCommand() {
        return false;
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String commandName, String[] args) {
        final BackupProfile config = ((YouBackupPlugin) mPlugin).getProfileConfiguration(mProfileName);
        if (config == null) {
            sender.sendMessage(YouBackupPlugin.TAG + "Unknown profile " + ChatUtils.colorized(ChatColor.BLUE, mProfileName) + " in YouBackup config.yml");
            return true;
        }
        mPlugin.getConfig().set(String.format("backups.%s.enabled", mProfileName), false);
        mPlugin.saveConfig();
        config.setEnabled(false);
        sender.sendMessage(String.format("The profile %s has been disabled", ChatUtils.colorized(ChatColor.BLUE, mProfileName)));
        return true;
    }

    @Override
    public boolean execute(Player player, Command command, String commandName, String[] args) {
        return execute((CommandSender) player, command, commandName, args);
    }
}
