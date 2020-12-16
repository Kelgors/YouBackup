package me.kelgors.youbackup;

import me.kelgors.youbackup.api.YouBackup;
import me.kelgors.youbackup.api.compression.ICompressor;
import me.kelgors.youbackup.api.storage.IStorage;
import me.kelgors.youbackup.commands.YouBackupCommandManager;
import me.kelgors.youbackup.commands.youbackup.profile.name.ProfileNameSubCommand;
import me.kelgors.youbackup.compression.ZipCompressor;
import me.kelgors.youbackup.configuration.BackupProfile;
import me.kelgors.youbackup.configuration.PluginConfiguration;
import me.kelgors.youbackup.extension.InstantiationUtils;
import me.kelgors.youbackup.extension.RegisteredExtension;
import me.kelgors.youbackup.extension.YouBackupManager;
import me.kelgors.youbackup.storage.FileStorage;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class YouBackupPlugin extends JavaPlugin {

    public static String TAG = String.format("[%sYouBackup%s] ", ChatColor.BLUE, ChatColor.RESET);
    private static final int PLUGIN_ID = 9567;

    // used by commands
    private YouBackupCommandManager mCommandManager;
    // used by Compression/Storage plugins
    private PluginConfiguration mConfiguration;
    private Metrics mMetrics;
    private YouBackupManager mYouBackupManager;

    //region Plugin API

    @Override
    public void onEnable() {
        super.onEnable();
        // configuration
        saveDefaultConfig();
        setLogLevelFromConfig();
        mConfiguration = new PluginConfiguration(getConfig());
        // metrics
        mMetrics = new Metrics(this, PLUGIN_ID);
        // prepare commands
        final PluginCommand command = getCommand("youbackup");
        if (command != null) {
            mCommandManager = new YouBackupCommandManager(this);
            command.setExecutor(mCommandManager);
            command.setTabCompleter(mCommandManager);
            loadCommands();
        }
        mYouBackupManager = new YouBackupManager(getDescription().getVersion());
        getServer().getServicesManager().register(YouBackup.class, mYouBackupManager, this, ServicePriority.Normal);
        // setup native extensions
        mYouBackupManager.registerCompression("zip", ZipCompressor.class, this);
        mYouBackupManager.registerStorage("file", FileStorage.class, this);
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
        if (mYouBackupManager != null) {
            mYouBackupManager.clear();
            mYouBackupManager = null;
        }
        mMetrics = null;
        mCommandManager = null;
        mConfiguration = null;
    }
    //endregion

    //region Commands
    public void loadCommands() {
        for (BackupProfile config : mConfiguration.getProfiles()) {
            mCommandManager.getProfileCommandManager().addSubCommand(config.getName(), new ProfileNameSubCommand(this, config.getName()));
        }
    }
    public void unloadCommands() {
        for (BackupProfile config : mConfiguration.getProfiles()) {
            mCommandManager.getProfileCommandManager().removeSubCommand(config.getName());
        }
    }
    //endregion

    public void reloadPluginConfig() {
        getLogger().info("Reload config...");
        getServer().getScheduler().cancelTasks(this);
        unloadCommands();
        this.reloadConfig();
        mConfiguration = new PluginConfiguration(getConfig());
        loadCommands();
        startCron();
    }

    //region Configuration
    public PluginConfiguration getConfiguration() {
        return mConfiguration;
    }

    public BackupProfile getProfileConfiguration(String profile) {
        return mConfiguration.getProfile(profile);
    }
    //endregion

    //region Save profile
    public CompletableFuture<Boolean> save(String profile) {
        return save(profile, null);
    }

    public CompletableFuture<Boolean> save(String profile, CommandSender sender) {
        final Logger logger = getLogger();
        logger.info(String.format("Running profile %s", profile));
        final BackupProfile config = mConfiguration.getProfile(profile);
        if (config == null) {
            logger.warning(String.format("Missing configuration with profile %s", profile));
            return CompletableFuture.completedFuture(false);
        }
        final String compressorType = config.getCompression().getString("type");
        final String storageType = config.getDestination().getString("type");
        final RegisteredExtension<ICompressor> CompressorExt = mYouBackupManager.getRegisteredCompressor(compressorType);
        final RegisteredExtension<IStorage> StorageExt = mYouBackupManager.getRegisteredStorage(storageType);

        if (CompressorExt == null) {
            logger.info(String.format("Unable to find compressor for %s", compressorType));
            return CompletableFuture.completedFuture(false);
        }
        if (StorageExt == null) {
            logger.info(String.format("Unable to find storage for %s", storageType));
            return CompletableFuture.completedFuture(false);
        }

        ICompressor compressor = null;
        IStorage storage = null;
        try {
            logger.info(String.format("Prepare compression with : %s", CompressorExt.getExtension()));
            compressor = InstantiationUtils.instantiateCompressor(CompressorExt.getExtension(), CompressorExt.getPlugin());
            logger.info(String.format("Prepare storage with : %s", StorageExt.getExtension()));
            storage = InstantiationUtils.instantiateStorage(StorageExt.getExtension(), StorageExt.getPlugin());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
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
        final boolean isVerbose = getConfig().getBoolean("verbose", false);
        if (isVerbose) {
            getLogger().setLevel(Level.FINEST);
        }
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
