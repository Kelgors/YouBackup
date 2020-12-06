package me.kelgors.youbackup.s3;

import me.kelgors.youbackup.api.YouBackup;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class YouBackupS3Plugin extends JavaPlugin {

    private static final int PLUGIN_ID = 9568;
    private YouBackup mYouBackup;
    private Metrics mMetrics;

    @Override
    public void onEnable() {
        super.onEnable();
        getLogger().warning("Add S3 storage to YouBackup");
        mMetrics = new Metrics(this, PLUGIN_ID);
        final RegisteredServiceProvider<YouBackup> serviceProvider = getServer().getServicesManager().getRegistration(YouBackup.class);
        if (serviceProvider != null) {
            mYouBackup = serviceProvider.getProvider();
            mYouBackup.registerStorage(S3Storage.STORAGE_TYPE, S3Storage.class, this);
        } else {
            getLogger().severe("Cannot found service provider YouBackup");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mMetrics = null;
        if (mYouBackup != null) {
            getLogger().info("Remove S3 storage from YouBackup");
            mYouBackup.unregisterStorage(S3Storage.STORAGE_TYPE, this);
            mYouBackup = null;
        }
    }
}
