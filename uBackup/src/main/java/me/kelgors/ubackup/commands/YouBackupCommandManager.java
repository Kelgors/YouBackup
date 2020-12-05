package me.kelgors.ubackup.commands;

import me.kelgors.ubackup.YouBackupPlugin;
import me.kelgors.ubackup.commands.ubackup.HelpSubCommand;
import me.kelgors.ubackup.commands.ubackup.ReloadSubCommand;
import me.kelgors.utils.commands.CommandManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class YouBackupCommandManager extends CommandManager<YouBackupPlugin> {

    public YouBackupCommandManager(YouBackupPlugin plugin) {
        super(plugin);
        addSubCommand("help", new HelpSubCommand());
        addSubCommand("reload", new ReloadSubCommand());
    }

    @Override
    protected boolean onCommandWithNoLabel(CommandSender sender, Command command) {
        return commands.get("help").execute(sender, command, "youbackup", new String[] { "help" });
    }
}
