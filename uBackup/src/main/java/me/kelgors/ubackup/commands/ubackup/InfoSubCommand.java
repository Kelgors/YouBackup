package me.kelgors.ubackup.commands.ubackup;

import me.kelgors.ubackup.WorldConfiguration;
import me.kelgors.ubackup.uBackupPlugin;
import me.kelgors.utils.chat.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InfoSubCommand extends AbsWorldRelatedSubCommand {
    @Override
    public boolean checkPermission(Player player) {
        return player.hasPermission("me.kelgors.me.kelgors.me.kelgors.ubackup.info") || player.hasPermission("me.kelgors.me.kelgors.me.kelgors.ubackup.*");
    }

    @Override
    public boolean isOnlyPlayerSubCommand() {
        return false;
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String commandName, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(uBackupPlugin.TAG + "Please specify a world name");
            return true;
        }
        final String worldName = args[0];
        final WorldConfiguration config = ((uBackupPlugin) mPlugin).getWorldConfiguration(worldName);
        if (config == null) {
            sender.sendMessage(uBackupPlugin.TAG + "Unknown world " + ChatUtils.colorized(ChatColor.BLUE, worldName) + " in uBackup config.yml");
            return true;
        }
        sender.sendMessage(new String[] {
                uBackupPlugin.TAG + "World(" + worldName + ")",
                "- enabled: " + ChatUtils.colorized((config.enabled ? ChatColor.GREEN : ChatColor.RED) , String.valueOf(config.enabled)),
                "- type: " + ChatUtils.colorized(ChatColor.GREEN, (String) config.destination.get("type"))
        });
        return true;
    }

    @Override
    public boolean execute(Player player, Command command, String commandName, String[] args) {
        return execute((CommandSender) player, command, commandName, args);
    }
}
