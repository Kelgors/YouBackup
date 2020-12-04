package me.kelgors.ubackup.commands.ubackup;

import me.kelgors.ubackup.uBackupPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HelpSubCommand extends AbsuBackupSubCommand {

    private final String HELP_MESSAGE = "\n  - help: display this message"
            + "\n  - now <profile>: Perform the backup profile now"
            + "\n  - enable|disable <profile>: Enable or disable a backup profile"
            + "\n  - info [profile]: Display backup information";

    @Override
    public boolean checkPermission(Player player) {
        return player.hasPermission("ubackup.help") || player.hasPermission("ubackup.*");
    }

    @Override
    public boolean isOnlyPlayerSubCommand() {
        return false;
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String commandName, String[] args) {
        sender.sendMessage(uBackupPlugin.TAG + mPlugin.getDescription().getVersion() + HELP_MESSAGE);
        return false;
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
