package me.kelgors.youbackup.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Configuration {

    List<BackupConfiguration> mBackups = new ArrayList<>();

    public Configuration(FileConfiguration configuration) {
        parse(configuration);
    }

    void parse(FileConfiguration configuration) {
        // configuration.getConfigurationSection("destinations");
        final ConfigurationSection backups = configuration.getConfigurationSection("backups");
        if (backups == null) {
            return;
        }
        final ArrayList<BackupConfiguration> backupList = new ArrayList<>();
        for (String name : backups.getKeys(false)) {
            ConfigurationSection section = backups.getConfigurationSection(name);
            if (section == null) continue;
            backupList.add(new BackupConfiguration(name, section));
        }
        mBackups = backupList;
    }

    public BackupConfiguration getConfiguration(String profile) {
        for (BackupConfiguration config : mBackups) {
            if (config.getName().equals(profile)) return config;
        }
        return null;
    }

    public List<BackupConfiguration> getConfigurations() {
        return mBackups;
    }

    public Long getNextCronAsTick() {
        Long output = null;
        for (BackupConfiguration config : mBackups) {
            if (!config.isEnabled()) continue;
            Long backupNextExecution = config.getNextExecutionRemainingTicks();
            if (backupNextExecution == null) continue;
            if (output == null) {
                output = backupNextExecution;
            } else if (output > backupNextExecution) {
                output = backupNextExecution;
            }
        }
        return output;
    }
}
