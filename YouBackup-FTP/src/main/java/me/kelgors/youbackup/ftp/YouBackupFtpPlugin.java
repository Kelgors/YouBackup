package me.kelgors.youbackup.ftp;

import me.kelgors.youbackup.api.YouBackup;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class YouBackupFtpPlugin extends JavaPlugin {

    public static String SERVER_TAG = "[YouBackup-FTP] ";
    private static final int PLUGIN_ID = 9569;
    private Metrics mMetrics;
    private YouBackup mYouBackup;

    @Override
    public void onEnable() {
        super.onEnable();
        getLogger().info("Add FTP storage to YouBackup");
        mMetrics = new Metrics(this, PLUGIN_ID);
        final RegisteredServiceProvider<YouBackup> serviceProvider = getServer().getServicesManager().getRegistration(YouBackup.class);
        if (serviceProvider != null) {
            mYouBackup = serviceProvider.getProvider();
            mYouBackup.registerStorage(FtpStorage.STORAGE_TYPE, FtpStorage.class, this);
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
            getLogger().info("Remove FTP storage from YouBackup");
            mYouBackup.unregisterStorage(FtpStorage.STORAGE_TYPE, this);
            mYouBackup = null;
        }
    }
}
