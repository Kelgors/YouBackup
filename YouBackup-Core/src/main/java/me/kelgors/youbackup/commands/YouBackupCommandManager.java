package me.kelgors.youbackup.commands;

import me.kelgors.utils.commands.AbsSubCommand;
import me.kelgors.utils.commands.CommandManager;
import me.kelgors.utils.commands.CommandUtils;
import me.kelgors.youbackup.YouBackupPlugin;
import me.kelgors.youbackup.commands.youbackup.HelpSubCommand;
import me.kelgors.youbackup.commands.youbackup.ProfileSubCommand;
import me.kelgors.youbackup.commands.youbackup.ReloadSubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class YouBackupCommandManager extends CommandManager<YouBackupPlugin> {

    public YouBackupCommandManager(YouBackupPlugin plugin) {
        super(plugin);
        addSubCommand("help", new HelpSubCommand());
        addSubCommand("reload", new ReloadSubCommand());
        addSubCommand("profile", new ProfileSubCommand(plugin));
    }

    @Override
    protected boolean onCommandWithNoLabel(CommandSender sender, Command command) {
        final AbsSubCommand subCommand = commands.get("help");
        if (sender instanceof Player && !subCommand.checkPermission((Player) sender)) {
            sender.sendMessage(CommandUtils.NO_PERMISSIONS);
            return true;
        }
        return subCommand.execute(sender, command, "help", new String[0]);
    }

    public ProfileSubCommand.ProfileSubCommandManager getProfileCommandManager() {
        return ((ProfileSubCommand) getCommand("profile")).getCommandManager();
    }
}
