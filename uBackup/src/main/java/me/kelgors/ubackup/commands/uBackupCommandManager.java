package me.kelgors.ubackup.commands;

import me.kelgors.ubackup.commands.ubackup.*;
import me.kelgors.ubackup.uBackupPlugin;
import me.kelgors.utils.commands.CommandManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class uBackupCommandManager extends CommandManager<uBackupPlugin> {

    public uBackupCommandManager(uBackupPlugin plugin) {
        super(plugin);
        addSubCommand("help", new HelpSubCommand());
        addSubCommand("now", new NowSubCommand());
        addSubCommand("enable", new EnableSubCommand());
        addSubCommand("disable", new DisableSubCommand());
        addSubCommand("info", new InfoSubCommand());
    }

    @Override
    protected boolean onCommandWithNoLabel(CommandSender sender, Command command) {
        return commands.get("help").execute(sender, command, "me/kelgors/ubackup", new String[] { "help" });
    }
}
