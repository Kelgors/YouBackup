package me.kelgors.ubackup.commands.ubackup;

import me.kelgors.ubackup.YouBackupPlugin;
import me.kelgors.ubackup.commands.ubackup.profile.DisableSubCommand;
import me.kelgors.ubackup.commands.ubackup.profile.EnableSubCommand;
import me.kelgors.ubackup.commands.ubackup.profile.InfoSubCommand;
import me.kelgors.ubackup.commands.ubackup.profile.NowSubCommand;
import me.kelgors.ubackup.configuration.BackupConfiguration;
import me.kelgors.ubackup.configuration.Configuration;
import me.kelgors.utils.commands.CommandManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
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
        return true;
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
        getPlugin().getLogger().info("Label: "+ label);
        return mManager.onTabComplete(commandSender, command, label, args);
//        final Configuration configurations = ((YouBackupPlugin) mPlugin).getConfiguration();
//        final List<String> output = new ArrayList<>();
//        final String arg0 = args.length > 0 ? args[0] : "";
//        for (BackupConfiguration config : configurations.getConfigurations()) {
//            boolean isCorresponding = "".equals(arg0) || (config.getName() != null && config.getName().startsWith(arg0));
//            if (isCorresponding) {
//                output.add(config.getName());
//            }
//        }
//        return output;
    }

    private static final class ProfileSubCommandManager extends CommandManager<YouBackupPlugin> {

        ProfileSubCommandManager(YouBackupPlugin plugin) {
            super(plugin);
        }

        @Override
        protected boolean onCommandWithNoLabel(CommandSender sender, Command command) {
            return commands.get("info").execute(sender, command, "info", new String[0]);
        }

        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
            return super.onTabComplete(commandSender, command, label, args);
        }
    }
}
