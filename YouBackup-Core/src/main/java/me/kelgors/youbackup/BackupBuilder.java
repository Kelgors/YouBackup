package me.kelgors.youbackup;

import me.kelgors.youbackup.api.compression.ICompressor;
import me.kelgors.youbackup.api.configuration.IBackupConfiguration;
import me.kelgors.youbackup.api.storage.IRemoteFile;
import me.kelgors.youbackup.api.storage.IStorage;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class BackupBuilder {

    final Plugin mPlugin;
    CommandSender mCommandSender;
    ICompressor mCompressor;
    IStorage mStorage;
    IBackupConfiguration mConfig;

    private List<String> mWorlds;

    public static BackupBuilder create(Plugin plugin) {
        return new BackupBuilder(plugin);
    }

    private BackupBuilder(Plugin plugin) {
        mPlugin = plugin;
    }

    public BackupBuilder setCommandSender(CommandSender sender) {
        mCommandSender = sender;
        return this;
    }

    public BackupBuilder setCompressor(ICompressor compressor) {
        mCompressor = compressor;
        return this;
    }

    public BackupBuilder setStorage(IStorage storage) {
        mStorage = storage;
        return this;
    }

    public BackupBuilder setProfile(IBackupConfiguration config) {
        mConfig = config;
        return this;
    }

    private void notifySender(String message) {
        mPlugin.getLogger().info(ChatColor.stripColor(YouBackupPlugin.TAG + message));
        if (mCommandSender == null || !(mCommandSender instanceof Player)) return;
        mCommandSender.sendMessage(YouBackupPlugin.TAG + message);
    }

    public CompletableFuture<Boolean> backup() {
        CompletableFuture<Boolean> output = new CompletableFuture<>();
        try {
            FileResolver resolver = new FileResolver(mPlugin, mConfig);
            mCompressor.prepare(mConfig);
            mStorage.prepare(mConfig);
            output = saveWorlds()
                .thenCompose(resolver::resolve)
                .thenCompose(this::compress)
                .thenCompose(this::store)
                .thenCompose(this::applyRotation);
            output.whenComplete(this::closeStorage);
            output.whenComplete(this::resetWorlds);
        } catch (Throwable ex) {
            ex.printStackTrace();
            output.completeExceptionally(ex);
        }
        return output;
    }

    //region Compression
    private CompletableFuture<File> compress(List<File> inputFileList) {
        notifySender("Compressing...");
        // prepare file
        final CompletableFuture<File> exOutput = new CompletableFuture<>();
        final String filename = FilenameFormatter.format(mConfig.getFilename(), LocalDateTime.now(), mConfig.getName());
        final File workDir = new File(mPlugin.getDataFolder(), "work");
        final File file = new File(workDir, File.separator + filename);
        try {
            if (!workDir.exists() &&!workDir.mkdir()) {
                mPlugin.getLogger().severe("Cannot create directory " + workDir.getAbsolutePath());
                exOutput.completeExceptionally(new Exception("Cannot create directory " + workDir.getAbsolutePath()));
                return exOutput;
            }
            if (!file.createNewFile()) {
                mPlugin.getLogger().severe("Cannot create file " + file.getAbsolutePath());
                exOutput.completeExceptionally(new Exception("Cannot create file " + file.getAbsolutePath()));
                return exOutput;
            }
        } catch (IOException e) {
            e.printStackTrace();
            exOutput.completeExceptionally(e);
            return exOutput;
        }
        return mCompressor.compress(inputFileList, file);
    }
    //endregion

    //region Storage
    private CompletableFuture<Boolean> store(File file) {
        notifySender("Uploading...");
        return mStorage.create(file);
    }
    private CompletableFuture<Boolean> delete(IRemoteFile file) {
        if (file == null) return CompletableFuture.completedFuture(true);
        notifySender(String.format("Deleting %s...", file.getName()));
        return mStorage.delete(file);
    }
    private CompletableFuture<Boolean> applyRotation(Boolean stored) {
        if (!stored) return CompletableFuture.completedFuture(false);
        if (mConfig.getRotation() <= 0) return CompletableFuture.completedFuture(true);
        notifySender("Checking file rotation...");
        return mStorage.list()
                .thenApply(this::getOlderFile)
                .thenCompose(this::delete);
    }
    private IRemoteFile getOlderFile(List<IRemoteFile> remoteFiles) {
        if (remoteFiles.size() <= mConfig.getRotation()) {
            return null;
        }
        final Optional<IRemoteFile> first = remoteFiles.stream().min((a, b) -> {
            boolean isOlder = a.getCreatedAt().isBefore(b.getCreatedAt());
            boolean isYounger = a.getCreatedAt().isAfter(b.getCreatedAt());
            if (isOlder) return -1;
            if (isYounger) return 1;
            return 0;
        });
        return first.orElse(null);
    }
    private void closeStorage(Boolean aBoolean, Throwable throwable) {
        mStorage.close();
    }
    //endregion

    //region World save & reset
    private CompletableFuture<List<File>> saveWorlds() {
        final CompletableFuture<List<File>> output = new CompletableFuture<>();
        mWorlds = new ArrayList<>(mConfig.getWorlds());
        if (mWorlds.size() > 0) {
            saveWorldAtAsync(0, output, new ArrayList<>());
        } else {
            output.complete(new ArrayList<>());
        }
        return output;
    }

    private void saveWorldAtAsync(final int index, final CompletableFuture<List<File>> future, final List<File> worldDirectories) {
        mPlugin.getServer().getScheduler().runTaskLater(mPlugin, () -> {
            final Logger logger = mPlugin.getLogger();
            final String worldName = mWorlds.get(index);
            final World world = mPlugin.getServer().getWorld(worldName);
            if (world != null) {
                // disable save until files are added to zip file
                logger.info("Disable autosave for world: " + worldName);
                world.setAutoSave(false);
                // save world to get the latest world commits
                logger.info("Save world: " + worldName);
                world.save();
                worldDirectories.add(world.getWorldFolder());
            } else {
                logger.warning(String.format("Skipping %s, world not found", worldName));
            }
            if (index + 1 < mWorlds.size()) {
                saveWorldAtAsync(index + 1, future, worldDirectories);
            } else {
                future.complete(worldDirectories);
            }
        }, 20);
    }

    private void resetWorlds(Boolean aBoolean, Throwable throwable) {
        final Logger logger = mPlugin.getLogger();
        for (String worldName : mWorlds) {
            World world = mPlugin.getServer().getWorld(worldName);
            if (world != null) {
                logger.info("Enable autosave back for world: " + worldName);
                world.setAutoSave(true);
            }
        }
    }
    //endregion
}
