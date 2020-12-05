package me.kelgors.ubackup.api.configuration;

import org.bukkit.configuration.ConfigurationSection;

public interface IBackupConfiguration {
    String getFilename();
    String getName();
    int getRotation();
    ConfigurationSection getCompression();
    ConfigurationSection getDestination();
    boolean isEnabled();
}
