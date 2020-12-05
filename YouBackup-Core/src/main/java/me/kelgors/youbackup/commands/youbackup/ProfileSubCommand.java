package me.kelgors.youbackup.commands.youbackup;

import me.kelgors.utils.commands.CommandManager;
import me.kelgors.youbackup.YouBackupPlugin;
import me.kelgors.youbackup.commands.youbackup.profile.DisableSubCommand;
import me.kelgors.youbackup.commands.youbackup.profile.EnableSubCommand;
import me.kelgors.youbackup.commands.youbackup.profile.InfoSubCommand;
import me.kelgors.youbackup.commands.youbackup.profile.NowSubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ProfileSubCommand extends AbsuBackupSubCommand {

    private ProfileSubCommandManager mManager;

    public ProfileSubCommand(YouBackupPlugin plugin, String profileName) {
        mManager = new ProfileSubCommandManager(plugin);
        mManager.addSubCommand("info", new InfoSubCommand(profileName));
        mManager.addSubCommand("now", new NowSubCommand(profileName));
        mManager.addSubCommand("enable", new EnableSubCommand(profileName));
        mManager.addSubCommand("disable", new DisableSubCommand(profileName));
    }

    @Override
    public boolean checkPermission(Player player) {
        return mManager.getCommand("info").checkPermission(player);
    }

    @Override
    public boolean isOnlyPlayerSubCommand() {
        return false;
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String commandName, String[] args) {
        return mManager.onCommand(sender, command, commandName, args);
    }

    @Override
    public boolean execute(Player player, Command command, String commandName, String[] args) {
        return execute((CommandSender) player, command, commandName, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        return mManager.onTabComplete(commandSender, command, label, args);
    }

    private static final class ProfileSubCommandManager extends CommandManager<YouBackupPlugin> {

        ProfileSubCommandManager(YouBackupPlugin plugin) {
            super(plugin);
        }

        @Override
        protected boolean onCommandWithNoLabel(CommandSender sender, Command command) {
            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
            return super.onTabComplete(commandSender, command, label, args);
        }
    }
}
