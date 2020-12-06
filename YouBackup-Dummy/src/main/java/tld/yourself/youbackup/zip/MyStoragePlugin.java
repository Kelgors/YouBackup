// MyStoragePlugin.java
package tld.yourself.youbackup.zip;

import me.kelgors.youbackup.api.YouBackup;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class MyStoragePlugin extends JavaPlugin {
    private YouBackup mYouBackup;

    @Override
    public void onEnable() {
        super.onEnable();
        // inform server owner that the plugin attempt to add a new storage to YouBackup
        getLogger().info(String.format("Add %s storage to YouBackup", MyStorage.STORAGE_TYPE));
        // get the YouBackup API
        final RegisteredServiceProvider<YouBackup> serviceProvider = getServer().getServicesManager().getRegistration(YouBackup.class);
        if (serviceProvider != null) {
            mYouBackup = serviceProvider.getProvider();
            // Register a new ICompressor
            mYouBackup.registerStorage(MyStorage.STORAGE_TYPE, MyStorage.class, this);
        } else {
            // If YouBackup is not present or cannot be enabled
            // inform server owner and disable this plugin
            getLogger().severe("Cannot found service provider YouBackup");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mYouBackup != null) {
            // inform server owner we will remove the storage
            getLogger().info(String.format("Remove %s storage from YouBackup", MyStorage.STORAGE_TYPE));
            // Unregister the compression you added earlier
            mYouBackup.unregisterCompression(MyStorage.STORAGE_TYPE, this);
            mYouBackup = null;
        }
    }
}
