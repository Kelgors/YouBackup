package me.kelgors.youbackup.api.configuration;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public interface IBackupConfiguration {
    String getFilename();
    String getName();
    int getRotation();
    ConfigurationSection getCompression();
    ConfigurationSection getDestination();
    boolean isEnabled();
    List<String> getWorlds();
    List<String> getIncludes();
    List<String> getExclude();
}
