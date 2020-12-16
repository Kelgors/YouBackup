package me.kelgors.youbackup.api.configuration;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public interface IBackupProfile {
    /**
     * Output file name pattern
     * @return
     */
    String getFilename();

    /**
     * Profile name
     * @return
     */
    String getName();

    /**
     * The maximum files on the remote service
     * @return
     */
    int getRotation();

    /**
     * "compression" section of yaml file
     * You can get any value in it.
     * You can ask to the user to add parameters like strength of compression
     * or anything you need to compress files
     * @return
     */
    ConfigurationSection getCompression();

    /**
     * "storage" section of yaml file
     * You can get any value in it
     * You can ask the user to add parameters like hostname, port
     * or anything you need to store the file where you want to
     * @return
     */
    ConfigurationSection getDestination();

    /**
     * Is the profile is enabled or not
     * @return
     */
    boolean isEnabled();

    /**
     * List of world names
     * @return
     */
    List<String> getWorlds();

    /**
     * List of path
     * @return
     */
    List<String> getIncludes();

    /**
     * List of path
     * @return
     */
    List<String> getExclude();
}
