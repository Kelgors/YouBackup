package me.kelgors.ubackup.commands.ubackup;

import me.kelgors.ubackup.WorldConfiguration;
import me.kelgors.ubackup.uBackupPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public abstract class AbsWorldRelatedSubCommand extends AbsuBackupSubCommand {

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length > 1) return new ArrayList<>();
        uBackupPlugin uBackupPlugin = (uBackupPlugin) mPlugin;
        final List<WorldConfiguration> configurations = uBackupPlugin.getWorldConfigurations();
        final List<String> output = new ArrayList<>();
        final String arg0 = args.length > 0 ? args[0] : "";
        for (WorldConfiguration config : configurations) {
            boolean isCorresponding = "".equals(arg0) || (config.name != null && config.name.startsWith(arg0));
            if (isCorresponding) {
                output.add(config.name);
            }
        }
        return output;
    }
}
