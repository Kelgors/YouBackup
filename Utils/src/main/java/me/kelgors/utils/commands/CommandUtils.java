package me.kelgors.utils.commands;

public class CommandUtils {

    public static boolean isBlank(String arg) {
        if (arg == null)
            return true;
        if (arg.trim().length() == 0)
            return true;
        return false;
    }

    public static boolean hasArgument(String[] args, int minArgs) {
        if (args == null)
            return false;
        if (args.length < minArgs)
            return false;
        return true;
    }

}