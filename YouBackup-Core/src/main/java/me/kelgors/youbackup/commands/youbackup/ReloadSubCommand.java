package me.kelgors.youbackup.commands.youbackup;

import me.kelgors.youbackup.YouBackupPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /yb reload
 */
public class ReloadSubCommand extends AbsYouBackupSubCommand {
    @Override
    public boolean checkPermission(Player player) {
        return player.hasPermission("youbackup.reload") || player.hasPermission("youbackup.*");
    }

    @Override
    public boolean isOnlyPlayerSubCommand() {
        return false;
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String commandName, String[] args) {
        ((YouBackupPlugin) getPlugin()).reloadPluginConfig();
        sender.sendMessage(YouBackupPlugin.TAG + "Configuration reloaded!");
        return true;
    }

    @Override
    public boolean execute(Player player, Command command, String commandName, String[] args) {
        return execute((CommandSender) player, command, commandName, args);
    }
}
