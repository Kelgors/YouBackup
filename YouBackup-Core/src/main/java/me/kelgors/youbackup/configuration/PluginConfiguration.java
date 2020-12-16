package me.kelgors.youbackup.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class PluginConfiguration {

    List<BackupProfile> mBackups = new ArrayList<>();

    public PluginConfiguration(FileConfiguration configuration) {
        parse(configuration);
    }

    void parse(FileConfiguration configuration) {
        // configuration.getConfigurationSection("destinations");
        final ConfigurationSection backups = configuration.getConfigurationSection("backups");
        if (backups == null) {
            return;
        }
        final ArrayList<BackupProfile> backupList = new ArrayList<>();
        for (String name : backups.getKeys(false)) {
            ConfigurationSection section = backups.getConfigurationSection(name);
            if (section == null) continue;
            backupList.add(new BackupProfile(name, section));
        }
        mBackups = backupList;
    }

    public BackupProfile getProfile(String profile) {
        for (BackupProfile config : mBackups) {
            if (config.getName().equals(profile)) return config;
        }
        return null;
    }

    public List<BackupProfile> getProfiles() {
        return mBackups;
    }

    public Long getNextCronAsTick() {
        Long output = null;
        for (BackupProfile config : mBackups) {
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
