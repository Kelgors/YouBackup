package me.kelgors.ubackup.commands.ubackup;

import me.kelgors.ubackup.uBackupPlugin;
import me.kelgors.utils.commands.AbsSubCommand;
import me.kelgors.utils.commands.CommandUtils;
import org.bukkit.entity.Player;

public abstract class AbsuBackupSubCommand extends AbsSubCommand {
    @Override
    protected boolean hasPermission(Player player, String permission) {
        return ((uBackupPlugin)mPlugin).getPermissions().has(player, permission);
    }

    protected Integer parseInt(String[] args, int index) {
        return parseInt(args, index, null);
    }

    protected Integer parseInt(String[] args, int index, Integer defaultValue) {
        try {
            return Integer.parseInt(parseString(args, index));
        } catch (NumberFormatException ex) {}
        return defaultValue;
    }

    protected String parseString(String[] args, int index) { return parseString(args, index, null); }

    protected String parseString(String[] args, int index, String defaultValue) {
        if (CommandUtils.hasArgument(args, index) && !CommandUtils.isBlank(args[index])) {
            return args[index];
        }
        return defaultValue;
    }
}
