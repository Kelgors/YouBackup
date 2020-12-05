package me.kelgors.youbackup.commands.youbackup;

import me.kelgors.youbackup.YouBackupPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HelpSubCommand extends AbsuBackupSubCommand {

    private final String HELP_MESSAGE = "\n  - help: display this message"
            + "\n  - reload: reload YouBackup configuration"
            + "\n  - <profile> now: Perform the backup profile now"
            + "\n  - <profile> enable|disable: Enable or disable a backup profile"
            + "\n  - <profile> info: Display backup information";

    @Override
    public boolean checkPermission(Player player) {
        return player.hasPermission("youbackup.help") || player.hasPermission("youbackup.*");
    }

    @Override
    public boolean isOnlyPlayerSubCommand() {
        return false;
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String commandName, String[] args) {
        sender.sendMessage(YouBackupPlugin.TAG + mPlugin.getDescription().getVersion() + HELP_MESSAGE);
        return true;
    }

    @Override
    public boolean execute(Player player, Command command, String commandName, String[] args) {
        return execute((CommandSender) player, command, commandName, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        return new ArrayList<>();
    }
}
