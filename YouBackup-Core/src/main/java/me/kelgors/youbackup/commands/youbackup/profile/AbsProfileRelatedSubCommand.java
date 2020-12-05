package me.kelgors.youbackup.commands.youbackup.profile;

import me.kelgors.youbackup.YouBackupPlugin;
import me.kelgors.youbackup.commands.youbackup.AbsuBackupSubCommand;
import me.kelgors.youbackup.configuration.BackupConfiguration;
import me.kelgors.youbackup.configuration.Configuration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public abstract class AbsProfileRelatedSubCommand extends AbsuBackupSubCommand {

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length > 1) return new ArrayList<>();
        YouBackupPlugin youBackupPlugin = (YouBackupPlugin) mPlugin;
        final Configuration configurations = youBackupPlugin.getConfiguration();
        final List<String> output = new ArrayList<>();
        final String arg0 = args.length > 0 ? args[0] : "";
        for (BackupConfiguration config : configurations.getConfigurations()) {
            boolean isCorresponding = "".equals(arg0) || (config.getName() != null && config.getName().startsWith(arg0));
            if (isCorresponding) {
                output.add(config.getName());
            }
        }
        return output;
    }
}
