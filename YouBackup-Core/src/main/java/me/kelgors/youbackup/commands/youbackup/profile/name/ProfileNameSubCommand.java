package me.kelgors.youbackup.commands.youbackup.profile.name;

import me.kelgors.utils.commands.AbsSubCommand;
import me.kelgors.utils.commands.CommandManager;
import me.kelgors.utils.commands.CommandUtils;
import me.kelgors.youbackup.YouBackupPlugin;
import me.kelgors.youbackup.commands.youbackup.AbsYouBackupSubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * /yb profile &lt;profile&gt;
 */
public class ProfileNameSubCommand extends AbsYouBackupSubCommand {
    private final String mProfileName;
    private final SubCommandManager mSubCommandManager;

    public ProfileNameSubCommand(YouBackupPlugin plugin, String profileName) {
        mProfileName = profileName;
        mSubCommandManager = new SubCommandManager(plugin);
        mSubCommandManager.addSubCommand("info", new InfoSubCommand(profileName));
        mSubCommandManager.addSubCommand("now", new NowSubCommand(profileName));
        mSubCommandManager.addSubCommand("enable", new EnableSubCommand(profileName));
        mSubCommandManager.addSubCommand("disable", new DisableSubCommand(profileName));
    }


    @Override
    public boolean checkPermission(Player player) {
        return CommandUtils.hasAnyPermission(player, new String[] {
                String.format("youbackup.%s.*", mProfileName),
                "youbackup.profile.*",
                "youbackup.*"
        }) || mSubCommandManager.getCommand("info").checkPermission(player);
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

    public static final class SubCommandManager extends CommandManager<YouBackupPlugin> {

        SubCommandManager(YouBackupPlugin plugin) {
            super(plugin);
        }

        @Override
        protected boolean onCommandWithNoLabel(CommandSender sender, Command command) {
            final boolean isPlayer = sender instanceof Player;
            final AbsSubCommand infoSubCommand = commands.get("info");
            if (!isPlayer) {
                return infoSubCommand.execute(sender, command, "info", new String[0]);
            } else if (infoSubCommand.checkPermission((Player) sender)) {
                return infoSubCommand.execute((Player) sender, command, "info", new String[0]);
            }
            sender.sendMessage(CommandUtils.NO_PERMISSIONS);
            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
            return super.onTabComplete(commandSender, command, label, args);
        }
    }
}
