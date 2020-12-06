package me.kelgors.youbackup;

import me.kelgors.youbackup.api.YouBackup;
import me.kelgors.youbackup.api.compression.ICompressor;
import me.kelgors.youbackup.api.storage.IStorage;
import me.kelgors.youbackup.commands.YouBackupCommandManager;
import me.kelgors.youbackup.commands.youbackup.profile.name.ProfileNameSubCommand;
import me.kelgors.youbackup.compression.ZipCompressor;
import me.kelgors.youbackup.configuration.BackupConfiguration;
import me.kelgors.youbackup.configuration.Configuration;
import me.kelgors.youbackup.storage.FileStorage;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class YouBackupPlugin extends JavaPlugin implements YouBackup {

    public static String TAG = String.format("[%sYouBackup%s] ", ChatColor.BLUE, ChatColor.RESET);
    private static final int PLUGIN_ID = 9567;

    // used by commands
    private YouBackupCommandManager mCommandManager;
    // used by save() and Compression/Storage API
    private Map<String, Class<? extends IStorage>> mStorage;
    private Map<String, Class<? extends ICompressor>> mCompressor;
    // used by Compression/Storage plugins
    private Configuration mConfiguration;
    private Metrics mMetrics;

    //region Plugin API
    @Override
    public void onLoad() {
        super.onLoad();
        mCompressor = new HashMap<>();
        mStorage = new HashMap<>();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        // configuration
        saveDefaultConfig();
        setLogLevelFromConfig();
        mConfiguration = new Configuration(getConfig());
        // metrics
        mMetrics = new Metrics(this, PLUGIN_ID);
        try {
            // load permissions for commands
//            if (!loadPermissions()) {
//                getLogger().warning("Unable to load permission from Vault. Please ensure you have Vault in your plugin folder");
//            }
            // prepare commands
            PluginCommand command = getCommand("youbackup");
            if (command != null) {
                mCommandManager = new YouBackupCommandManager(this);
                command.setExecutor(mCommandManager);
                command.setTabCompleter(mCommandManager);
                loadCommands();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            getPluginLoader().disablePlugin(this);
            return;
        }
        // setup native extensions
        setCompression("zip", ZipCompressor.class);
        setStorage("file", FileStorage.class);
        // start cron when all plugins are loaded
        getServer().getScheduler().scheduleSyncDelayedTask(this, this::startCron);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        getServer().getScheduler().cancelTasks(this);
        final PluginCommand command = getCommand("youbackup");
        if (command != null) {
            command.setExecutor(null);
            command.setTabCompleter(null);
        }
        mMetrics = null;
        mCommandManager = null;
        mConfiguration = null;
        mCompressor.clear();
        mStorage.clear();
    }
    //endregion

    //region Commands
    public void loadCommands() {
        for (BackupConfiguration config : mConfiguration.getConfigurations()) {
            mCommandManager.getProfileCommandManager().addSubCommand(config.getName(), new ProfileNameSubCommand(this, config.getName()));
        }
    }
    public void unloadCommands() {
        for (BackupConfiguration config : mConfiguration.getConfigurations()) {
            mCommandManager.getProfileCommandManager().removeSubCommand(config.getName());
        }
    }
    //endregion

    public void reloadPluginConfig() {
        getLogger().info("Reload config...");
        getServer().getScheduler().cancelTasks(this);
        unloadCommands();
        this.reloadConfig();
        mConfiguration = new Configuration(getConfig());
        loadCommands();
        startCron();
    }

    //region Compression/Storage API
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
    //endregion

    //region Configuration
    public Configuration getConfiguration() {
        return mConfiguration;
    }

    public BackupConfiguration getProfileConfiguration(String profile) {
        return mConfiguration.getConfiguration(profile);
    }
    //endregion

    //region Save profile
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
        Class<? extends ICompressor> CompressorKlass = mCompressor.get(config.getCompression().getString("type"));
        Class<? extends IStorage> StorageKlass = mStorage.get(config.getDestination().getString("type"));

        ICompressor compressor = null;
        IStorage storage = null;
        if (CompressorKlass == null) {
            logger.info(String.format("Unable to find compressor for %s", config.getCompression()));
            return CompletableFuture.completedFuture(false);
        }
        if (StorageKlass == null) {
            logger.info(String.format("Unable to find storage for %s", (String) config.getDestination().get("type")));
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
    //endregion

    //region utils
    private void setLogLevelFromConfig() {
        final String logLevel = getConfig().getString("log_level", "INFO");
        getLogger().info(String.format("Found Level.%s", logLevel));
        assert logLevel != null;
        getLogger().setLevel(Level.parse(logLevel.toUpperCase()));
    }
    //endregion

    //region startCron
    void startCron() {
        final Long remainingTicks = mConfiguration.getNextCronAsTick();
        if (remainingTicks == null) {
            getLogger().info("No backup profile to start");
            return;
        }
        getLogger().info(String.format("Prepare next cron to be executed in %d ticks (%d seconds)", remainingTicks, remainingTicks / 20));
        getServer().getScheduler().runTaskLater(this, new CronBackupRunner(this), remainingTicks + 20);
    }
    //endregion
}
