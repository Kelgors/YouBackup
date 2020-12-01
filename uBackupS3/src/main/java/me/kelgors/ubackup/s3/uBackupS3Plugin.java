package me.kelgors.ubackup.s3;

import me.kelgors.ubackup.uBackupPlugin;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class uBackupS3Plugin extends JavaPlugin {

    private static final int PLUGIN_ID = 9540;
    private uBackupPlugin muBackupPlugin;
    private Metrics mMetrics;

    @Override
    public void onEnable() {
        super.onEnable();
        getLogger().warning("Add S3 storage to uBackup");
        mMetrics = new Metrics(this, PLUGIN_ID);
        muBackupPlugin = (uBackupPlugin) getServer().getPluginManager().getPlugin("uBackup");
        if (muBackupPlugin != null) {
            muBackupPlugin.setStorage(S3Storage.STORAGE_TYPE, S3Storage.class);
        } else {
            getLogger().warning("Unable to find uBackup plugin. Cannot add S3 storage.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mMetrics = null;
        if (muBackupPlugin != null) {
            getLogger().warning("Remove S3 storage to uBackup");
            muBackupPlugin.removeStorage(S3Storage.STORAGE_TYPE);
            muBackupPlugin= null;
        }
    }
}
