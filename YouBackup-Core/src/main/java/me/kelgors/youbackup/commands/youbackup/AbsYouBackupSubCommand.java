package me.kelgors.youbackup.commands.youbackup;

import me.kelgors.utils.commands.AbsSubCommand;
import me.kelgors.utils.commands.CommandUtils;

public abstract class AbsYouBackupSubCommand extends AbsSubCommand {

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
