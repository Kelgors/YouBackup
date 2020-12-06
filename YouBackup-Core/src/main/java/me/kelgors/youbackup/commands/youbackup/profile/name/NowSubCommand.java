package me.kelgors.youbackup.commands.youbackup.profile.name;

import me.kelgors.utils.chat.ChatUtils;
import me.kelgors.utils.commands.CommandUtils;
import me.kelgors.youbackup.YouBackupPlugin;
import me.kelgors.youbackup.commands.youbackup.AbsYouBackupSubCommand;
import me.kelgors.youbackup.configuration.BackupConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /yb profile &lt;profile&gt; now
 */
public class NowSubCommand extends AbsYouBackupSubCommand {
    private final String mProfileName;

    public NowSubCommand(String profileName) {
        mProfileName = profileName;
    }

    @Override
    public boolean checkPermission(Player player) {
        return CommandUtils.hasAnyPermission(player, new String[] {
                String.format("youbackup.%s.now", mProfileName),
                String.format("youbackup.%s.*", mProfileName),
                "youbackup.*"
        });
    }

    @Override
    public boolean isOnlyPlayerSubCommand() {
        return false;
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String commandName, String[] args) {
        BackupConfiguration config = ((YouBackupPlugin)mPlugin).getProfileConfiguration(mProfileName);
        if (config == null) {
            sender.sendMessage(YouBackupPlugin.TAG + "Unknown profile " + ChatUtils.colorized(ChatColor.BLUE, mProfileName) + " in YouBackup config.yml");
            return true;
        }

        ((YouBackupPlugin) mPlugin).save(mProfileName, sender)
            .whenComplete((result, throwable) -> {
                sender.sendMessage(String.format("%sBackup(result: %s)", YouBackupPlugin.TAG, ChatUtils.colorized(result ? ChatColor.GREEN : ChatColor.RED, String.valueOf(result))));
                if (throwable != null) {
                    sender.sendMessage(YouBackupPlugin.TAG + ChatUtils.colorized(ChatColor.RED, throwable.getLocalizedMessage()));
                }
            });
        return true;
    }

    @Override
    public boolean execute(Player player, Command command, String commandName, String[] args) {
        return execute((CommandSender) player, command, commandName, args);
    }
}
