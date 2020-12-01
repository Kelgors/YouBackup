package me.kelgors.utils.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 *
 * @author Kelgors
 */
public abstract class AbsSubCommand implements TabCompleter {

    protected Plugin mPlugin;
    protected CommandManager mCommandManager;

    public Plugin getPlugin() { return mPlugin; }
    /**
     * set plugin reference
     * @param plugin
     */
    void setPlugin(Plugin plugin) {
        mPlugin = plugin;
    }

    /**
     * set a reference to the commandManager for shared functions
     * @param cm
     */
    void setCommandManager(CommandManager<?> cm) {
        mCommandManager = cm;
    }

    /**
     * Check minimum permissions to execute this command
     * Or send message to the player
     * @param player
     * @return
     */
    public abstract boolean checkPermission(Player player);

    /**
     * Check if this command can only be used by player
     * @return
     */
    public abstract boolean isOnlyPlayerSubCommand();

    /**
     * Execute a command
     * @param sender instance of BlockCommandSender|ConsoleCommandSender|Player|RemoteConsoleCommandSender
     * @param command
     * @param commandName
     * @param args
     * @return
     */
    public abstract boolean execute(CommandSender sender, Command command, String commandName, String[] args);
    /**
     * Execute a playerOnly command
     * @param player
     * @param command
     * @param commandName
     * @param args
     * @return
     */
    public abstract boolean execute(Player player, Command command, String commandName, String[] args);

    protected abstract boolean hasPermission(Player player, String permission);

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        return null;
    }
}

