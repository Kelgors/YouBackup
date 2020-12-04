package me.kelgors.ubackup;

import me.kelgors.ubackup.commands.uBackupCommandManager;
import me.kelgors.ubackup.compression.ICompressor;
import me.kelgors.ubackup.compression.ZipCompressor;
import me.kelgors.ubackup.configuration.BackupConfiguration;
import me.kelgors.ubackup.configuration.Configuration;
import me.kelgors.ubackup.storage.FileStorage;
import me.kelgors.ubackup.storage.IStorage;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class uBackupPlugin extends JavaPlugin {

    public static String TAG = String.format("[%suBackup%s] ", ChatColor.BLUE, ChatColor.RESET);
    public static String SERVER_TAG = "[Server] ";
    private static uBackupPlugin __instance__;

    public static uBackupPlugin getInstance() {
        return __instance__;
    }

    private Permission mPermissionManager;
    private Map<String, Class<? extends IStorage>> mStorage;
    private Map<String, Class<? extends ICompressor>> mCompressor;
    private Configuration mConfiguration;
    private static final int PLUGIN_ID = 9539;
    private Metrics mMetrics;


    @Override
    public void onLoad() {
        super.onLoad();
        __instance__ = this;
        getLogger().info("Loading uBackup...");
        mCompressor = new HashMap<>();
        mStorage = new HashMap<>();
    }

    private boolean loadPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
        if (permissionProvider != null) {
            mPermissionManager = permissionProvider.getProvider();
        }
        return (mPermissionManager != null);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        getLogger().info("Enable uBackup...");
        saveDefaultConfig();
        setLogLevelFromConfig();
        mMetrics = new Metrics(this, PLUGIN_ID);

        try {
            if (!loadPermissions()) {
                getLogger().warning("Unable to load permission from Vault. Please ensure you have Vault in your plugin folder");
            }
            PluginCommand command = getCommand("ubackup");
            if (command != null) {
                uBackupCommandManager commandManager = new uBackupCommandManager(this);
                command.setExecutor(commandManager);
                command.setTabCompleter(commandManager);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            getPluginLoader().disablePlugin(this);
            return;
        }

        mConfiguration = new Configuration(getConfig());
        setCompression("zip", ZipCompressor.class);
        setStorage("file", FileStorage.class);
        // setStorage("ftp", new FtpStorage(this));
    }

    @Override
    public void onDisable() {
        super.onDisable();
        getLogger().info("Disable uBackup...");
        if (mMetrics != null) mMetrics = null;
        getServer().getScheduler().cancelTasks(this);
        mPermissionManager = null;
        mConfiguration = null;
        PluginCommand command = getCommand("ubackup");
        if (command != null) {
            command.setExecutor(null);
            command.setTabCompleter(null);
        }
        mCompressor.clear();
        mStorage.clear();
    }

    public Permission getPermissions() {
        return mPermissionManager;
    }

    public void setStorage(String type, Class<? extends IStorage> storage) {
        mStorage.put(type, storage);
    }

    public void setCompression(String type, Class<? extends ICompressor> compressor) {
        mCompressor.put(type, compressor);
    }

    public void removeStorage(String type) {
        mStorage.remove(type);
    }

    public void removeCompression(String type) {
        mCompressor.remove(type);
    }

    public Configuration getConfiguration() {
        return mConfiguration;
    }

    public BackupConfiguration getProfileConfiguration(String profile) {
        return mConfiguration.getConfiguration(profile);
    }

    public CompletableFuture<Boolean> save(String profile) {
        return save(profile, null);
    }

    public CompletableFuture<Boolean> save(String profile, CommandSender sender) {
        final BackupConfiguration config = mConfiguration.getConfiguration(profile);
        final Logger logger = getLogger();
        if (config == null) {
            logger.warning(String.format("Missing configuration with profile %s", profile));
            return CompletableFuture.completedFuture(false);
        }
        Class<? extends ICompressor> CompressorKlass = mCompressor.get((String) config.compression.get("type"));
        Class<? extends IStorage> StorageKlass = mStorage.get((String) config.destination.get("type"));

        ICompressor compressor = null;
        IStorage storage = null;
        if (CompressorKlass == null) {
            logger.info(String.format("Unable to find compressor for %s", config.compression));
            return CompletableFuture.completedFuture(false);
        }
        if (StorageKlass == null) {
            logger.info(String.format("Unable to find storage for %s", (String) config.destination.get("type")));
            return CompletableFuture.completedFuture(false);
        }
        try {
            logger.info(String.format("Prepare compression with : %s", CompressorKlass.getCanonicalName()));
            compressor = CompressorKlass.getDeclaredConstructor(Plugin.class).newInstance(this);
            logger.info(String.format("Prepare storage with : %s", StorageKlass.getCanonicalName()));
            storage = StorageKlass.getDeclaredConstructor(Plugin.class).newInstance(this);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        if (compressor == null || storage == null) return CompletableFuture.completedFuture(false);
        return BackupBuilder.create(this)
                .setCompressor(compressor)
                .setStorage(storage)
                .setProfile(config)
                .setCommandSender(sender)
                .backup();
    }

    private void setLogLevelFromConfig() {
        final String logLevel = getConfig().getString("log_level", "INFO");
        assert logLevel != null;
        getLogger().setLevel(Level.parse(logLevel.toUpperCase()));
    }

}
