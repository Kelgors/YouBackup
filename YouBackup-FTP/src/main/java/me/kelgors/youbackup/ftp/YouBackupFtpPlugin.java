package me.kelgors.youbackup.ftp;

import me.kelgors.youbackup.api.YouBackup;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class YouBackupFtpPlugin extends JavaPlugin {

    public static String SERVER_TAG = "[YouBackup-FTP] ";
    private static final int PLUGIN_ID = 9569;
    private Metrics mMetrics;
    private YouBackup mYouBackup;

    @Override
    public void onEnable() {
        super.onEnable();
        getLogger().warning("Add FTP storage to YouBackup");
        mMetrics = new Metrics(this, PLUGIN_ID);
        mYouBackup = (YouBackup) getServer().getPluginManager().getPlugin("YouBackup");
        if (mYouBackup != null) {
            mYouBackup.setStorage(FtpStorage.STORAGE_TYPE, FtpStorage.class);
        } else {
            getLogger().warning("Unable to find YouBackup plugin. Cannot add FTP storage.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mMetrics = null;
        if (mYouBackup != null) {
            getLogger().warning("Remove FTP storage to YouBackup");
            mYouBackup.removeStorage(FtpStorage.STORAGE_TYPE);
            mYouBackup = null;
        }
    }
}
