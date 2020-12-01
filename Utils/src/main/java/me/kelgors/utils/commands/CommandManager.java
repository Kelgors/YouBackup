package me.kelgors.utils.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

/**
 *
 * @author Kelgors
 */
public abstract class CommandManager<T extends Plugin> implements CommandExecutor, TabCompleter {

    protected final static String NOT_PLAYER = "This is only a player command !";

    private T mBasePlugin;
    protected Map<String, AbsSubCommand> commands = new HashMap<>();

    public CommandManager(T plugin) {
        mBasePlugin = plugin;
    }

    /**
     * The method called by Bukkit
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandName,
                             String[] args) {
        String label = null;
        if (args.length > 0) {
            label = args[0];
        }
        if (label == null) {
            return this.onCommandWithNoLabel(sender, command);
        }
        // if there is a label
        AbsSubCommand sc = commands.get(label);
        if (sc != null) {
            if (sc.isOnlyPlayerSubCommand()) {
                if (sender instanceof Player && sc.checkPermission((Player) sender)) {
                    return sc.execute((Player) sender, command, label, Arrays.copyOfRange(args, 1, args.length));
                } else {
                    sender.sendMessage(ChatColor.RED + NOT_PLAYER);
                }
            } else {
                return sc.execute(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            }
        }
        return true;
    }

    /**
     * Add a subCommand to this command
     * @param label    The subcommand label
     * @param command  The SubCommand executor
     */
    protected void addSubCommand(String label, AbsSubCommand command) {
        if (command != null) {
            command.setPlugin(mBasePlugin);
            command.setCommandManager(this);
            if (commands.get(label) != null) {
                mBasePlugin.getLogger().warning("Command " + label + " overriden");
            }
            this.commands.put(label, command);
        }
    }

    /**
     * Get a command
     * @param key
     * @return
     */
    public AbsSubCommand getCommand(String key) {
        return commands.get(key);
    }

    protected T getPlugin() {
        return mBasePlugin;
    }

    /**
     * Performs this command when no args was specified
     * @param sender
     * @param command
     * @return
     */
    protected abstract boolean onCommandWithNoLabel(CommandSender sender, Command command);

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        mBasePlugin.getLogger().info(Arrays.toString(args));
        if (command == null || args.length == 0)  return null;
        final Set<String> set = commands.keySet();
        final List<String> output = new ArrayList<>();
        final String arg0 = args[0];
        for (String commandName : set) {
            boolean is0Empty = "".equals(arg0);
            if (is0Empty || commandName.startsWith(arg0)) {
                if (!is0Empty && args.length > 1) {
                    return commands.get(commandName).onTabComplete(commandSender, command, arg0, Arrays.copyOfRange(args, 1, args.length));
                }
                output.add(commandName);
            }
        }
        return output;
    }
}


