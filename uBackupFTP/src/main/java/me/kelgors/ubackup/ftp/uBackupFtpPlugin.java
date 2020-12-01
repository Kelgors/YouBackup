package me.kelgors.ubackup.ftp;

import me.kelgors.ubackup.uBackupPlugin;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class uBackupFtpPlugin extends JavaPlugin {

    private static final int PLUGIN_ID = 9541;
    private Metrics mMetrics;
    private uBackupPlugin muBackupPlugin;

    @Override
    public void onEnable() {
        super.onEnable();
        getLogger().warning("Add S3 storage to uBackup");
        mMetrics = new Metrics(this, PLUGIN_ID);
        muBackupPlugin = (uBackupPlugin) getServer().getPluginManager().getPlugin("uBackup");
        if (muBackupPlugin != null) {
            muBackupPlugin.setStorage(FtpStorage.STORAGE_TYPE, FtpStorage.class);
        } else {
            getLogger().warning("Unable to find uBackup plugin. Cannot add FTP storage.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mMetrics = null;
        if (muBackupPlugin != null) {
            getLogger().warning("Remove FTP storage to uBackup");
            muBackupPlugin.removeStorage(FtpStorage.STORAGE_TYPE);
            muBackupPlugin= null;
        }
    }
}
