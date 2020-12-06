package me.kelgors.youbackup.commands.youbackup;

import me.kelgors.utils.commands.CommandManager;
import me.kelgors.youbackup.YouBackupPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * /yb profile ...
 */
public class ProfileSubCommand extends AbsYouBackupSubCommand {

    private final ProfileSubCommandManager mSubCommandManager;

    public ProfileSubCommand(YouBackupPlugin plugin) {
        mSubCommandManager = new ProfileSubCommandManager(plugin);
    }

    @Override
    public boolean checkPermission(Player player) {
        return true;
    }

    @Override
    public boolean isOnlyPlayerSubCommand() {
        return false;
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String commandName, String[] args) {
        return mSubCommandManager.onCommand(sender, command, commandName, args);
    }

    @Override
    public boolean execute(Player player, Command command, String commandName, String[] args) {
        return execute((CommandSender) player, command, commandName, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        return mSubCommandManager.onTabComplete(commandSender, command, label, args);
    }

    public ProfileSubCommandManager getCommandManager() {
        return mSubCommandManager;
    }

    public static final class ProfileSubCommandManager extends CommandManager<YouBackupPlugin> {

        ProfileSubCommandManager(YouBackupPlugin plugin) {
            super(plugin);
        }

        @Override
        protected boolean onCommandWithNoLabel(CommandSender sender, Command command) {
            return true;
        }
    }
}
