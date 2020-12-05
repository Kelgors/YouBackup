package me.kelgors.ubackup.s3;

import me.kelgors.ubackup.api.YouBackup;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class YouBackupS3Plugin extends JavaPlugin {

    private static final int PLUGIN_ID = 9540;
    private YouBackup mYouBackup;
    private Metrics mMetrics;

    @Override
    public void onEnable() {
        super.onEnable();
        getLogger().warning("Add S3 storage to YouBackup");
        mMetrics = new Metrics(this, PLUGIN_ID);
        mYouBackup = (YouBackup) getServer().getPluginManager().getPlugin("YouBackup");
        if (mYouBackup != null) {
            mYouBackup.setStorage(S3Storage.STORAGE_TYPE, S3Storage.class);
        } else {
            getLogger().warning("Unable to find YouBackup plugin. Cannot add S3 storage.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mMetrics = null;
        if (mYouBackup != null) {
            getLogger().warning("Remove S3 storage from YouBackup");
            mYouBackup.removeStorage(S3Storage.STORAGE_TYPE);
            mYouBackup= null;
        }
    }
}
