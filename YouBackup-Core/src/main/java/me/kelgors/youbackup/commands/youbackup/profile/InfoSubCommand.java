package me.kelgors.youbackup.commands.youbackup.profile;

import me.kelgors.utils.chat.ChatUtils;
import me.kelgors.youbackup.YouBackupPlugin;
import me.kelgors.youbackup.configuration.BackupConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.format.DateTimeFormatter;

public class InfoSubCommand extends AbsProfileRelatedSubCommand {
    private final String mProfileName;

    public InfoSubCommand(String profileName) {
        mProfileName = profileName;
    }

    @Override
    public boolean checkPermission(Player player) {
        return player.hasPermission("youbackup.info") || player.hasPermission("youbackup.*");
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
        sender.sendMessage(new String[] {
                YouBackupPlugin.TAG + "Profile(" + mProfileName + ")",
                "- enabled: " + ChatUtils.colorized((config.isEnabled() ? ChatColor.GREEN : ChatColor.RED), String.valueOf(config.isEnabled())),
                "- next: " + ChatUtils.colorized(ChatColor.GREEN, config.getNextExecutionTime().format(DateTimeFormatter.ISO_DATE_TIME)),
                "- compression: " + ChatUtils.colorized(ChatColor.GREEN, config.getCompression().getString("type", "<unknown>")),
                "- destination: " + ChatUtils.colorized(ChatColor.GREEN, (String) config.getDestination().get("type"))
        });
        return true;
    }

    @Override
    public boolean execute(Player player, Command command, String commandName, String[] args) {
        return execute((CommandSender) player, command, commandName, args);
    }
}
