package me.kelgors.ubackup.configuration;

import org.bukkit.configuration.ConfigurationSection;

public class BackupConfiguration {

    public String filename;
    public String name;
    public String cron = null;
    public int rotation = 0;
    public boolean enabled = false;
    public ConfigurationSection compression;
    public ConfigurationSection destination;

    public static BackupConfiguration parse(String name, ConfigurationSection section) {
        // section.isString("destination")
        return new BackupConfiguration(name, section.getString("filename", "{uuid}.zip"),
                section.getBoolean("enabled", false),
                section.getInt("rotation", 0),
                section.getString("cron", null),
                section.getConfigurationSection("compression"),
                section.getConfigurationSection("destination")
        );
    }

    public BackupConfiguration(String name, String filename, boolean enabled, int rotation, String cron, ConfigurationSection compression, ConfigurationSection destination) {
        this.name = name;
        this.filename = filename;
        this.enabled = enabled;
        this.rotation = rotation;
        this.cron = cron;
        this.compression = compression;
        this.destination = destination;

    }
}
