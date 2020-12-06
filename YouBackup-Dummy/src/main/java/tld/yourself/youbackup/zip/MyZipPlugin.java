// MyZipPlugin.java
package tld.yourself.youbackup.zip;

import me.kelgors.youbackup.api.YouBackup;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class MyZipPlugin extends JavaPlugin {

    private YouBackup mYouBackup;

    @Override
    public void onEnable() {
        super.onEnable();
        // inform server owner that the plugin attempt to add a new compression to YouBackup
        getLogger().info(String.format("Add %s compression to YouBackup", MyZipCompressor.COMPRESSOR_TYPE));
        // get the YouBackup API
        final RegisteredServiceProvider<YouBackup> serviceProvider = getServer().getServicesManager().getRegistration(YouBackup.class);
        if (serviceProvider != null) {
            mYouBackup = serviceProvider.getProvider();
            // Register a new ICompressor
            mYouBackup.registerCompression(MyZipCompressor.COMPRESSOR_TYPE, MyZipCompressor.class, this);
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
            // inform server owner we will remove the zip compression
            getLogger().info(String.format("Remove %s compression from YouBackup", MyZipCompressor.COMPRESSOR_TYPE));
            // Unregister the compression you added earlier
            mYouBackup.unregisterCompression(MyZipCompressor.COMPRESSOR_TYPE, this);
            mYouBackup = null;
        }
    }
}
