package me.kelgors.youbackup.commands;

import me.kelgors.utils.commands.AbsSubCommand;
import me.kelgors.utils.commands.CommandManager;
import me.kelgors.youbackup.YouBackupPlugin;
import me.kelgors.youbackup.commands.youbackup.HelpSubCommand;
import me.kelgors.youbackup.commands.youbackup.ReloadSubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class YouBackupCommandManager extends CommandManager<YouBackupPlugin> {

    public YouBackupCommandManager(YouBackupPlugin plugin) {
        super(plugin);
        addSubCommand("help", new HelpSubCommand());
        addSubCommand("reload", new ReloadSubCommand());
    }

    @Override
    protected boolean onCommandWithNoLabel(CommandSender sender, Command command) {
        AbsSubCommand subCommand = commands.get("help");
        if (sender instanceof Player && !subCommand.checkPermission((Player) sender)) {
            return false;
        }
        return subCommand.execute(sender, command, "help", new String[0]);
    }
}
