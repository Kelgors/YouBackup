package me.kelgors.ubackup.commands.ubackup;

import me.kelgors.ubackup.configuration.BackupConfiguration;
import me.kelgors.ubackup.uBackupPlugin;
import me.kelgors.utils.chat.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NowSubCommand extends AbsWorldRelatedSubCommand {
    @Override
    public boolean checkPermission(Player player) {
        return player.hasPermission("ubackup.now") || player.hasPermission("ubackup.*");
    }

    @Override
    public boolean isOnlyPlayerSubCommand() {
        return false;
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String commandName, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(uBackupPlugin.TAG + "Missing worldName. /ubackup now <world_name>");
            return true;
        }

        final String profileName = args[0];
        BackupConfiguration config = ((uBackupPlugin)mPlugin).getProfileConfiguration(profileName);
        if (config == null) {
            sender.sendMessage(uBackupPlugin.TAG + "Unknown profile " + ChatUtils.colorized(ChatColor.BLUE, profileName) + " in uBackup config.yml");
            return true;
        }
        uBackupPlugin.getInstance().save(profileName, sender)
            .whenComplete((result, throwable) -> {
                sender.sendMessage(String.format("%sBackup(result: %s)", uBackupPlugin.TAG, ChatUtils.colorized(result ? ChatColor.GREEN : ChatColor.RED, String.valueOf(result))));
                if (throwable != null) {
                    sender.sendMessage(uBackupPlugin.TAG + ChatUtils.colorized(ChatColor.RED, throwable.getLocalizedMessage()));
                }
            });
        return true;
    }

    @Override
    public boolean execute(Player player, Command command, String commandName, String[] args) {
        return execute((CommandSender) player, command, commandName, args);
    }
}
