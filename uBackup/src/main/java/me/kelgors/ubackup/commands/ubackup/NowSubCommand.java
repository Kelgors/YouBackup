package me.kelgors.ubackup.commands.ubackup;

import me.kelgors.ubackup.WorldConfiguration;
import me.kelgors.ubackup.uBackupPlugin;
import me.kelgors.utils.chat.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NowSubCommand extends AbsWorldRelatedSubCommand {
    @Override
    public boolean checkPermission(Player player) {
        return player.hasPermission("me.kelgors.me.kelgors.me.kelgors.ubackup.now") || player.hasPermission("me.kelgors.me.kelgors.me.kelgors.ubackup.*");
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

        final String worldName = args[0];
        WorldConfiguration config = ((uBackupPlugin) mPlugin).getWorldConfiguration(worldName);
        World world = getPlugin().getServer().getWorld(worldName);
        if (config == null) {
            sender.sendMessage(uBackupPlugin.TAG + "Unknown world " + ChatUtils.colorized(ChatColor.BLUE, worldName) + " in uBackup config.yml");
            return true;
        }
        if (world == null) {
            sender.sendMessage(uBackupPlugin.TAG + "World " + ChatUtils.colorized(ChatColor.BLUE, worldName) + " does not exists on your server");
            return true;
        }
        uBackupPlugin.getInstance().save(world, sender)
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
