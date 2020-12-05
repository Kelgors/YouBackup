package me.kelgors.ubackup;

import me.kelgors.ubackup.compression.ICompressor;
import me.kelgors.ubackup.configuration.BackupConfiguration;
import me.kelgors.ubackup.storage.IStorage;
import me.kelgors.ubackup.storage.RemoteFile;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class BackupBuilder {

    final Plugin mPlugin;
    CommandSender mCommandSender;
    ICompressor mCompressor;
    IStorage mStorage;
    BackupConfiguration mConfig;

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

    public BackupBuilder setProfile(BackupConfiguration config) {
        mConfig = config;
        return this;
    }

    private void notifySender(String message) {
        mPlugin.getLogger().info(uBackupPlugin.TAG + message);
        if (mCommandSender == null || !(mCommandSender instanceof Player)) return;
        mCommandSender.sendMessage(uBackupPlugin.TAG + message);
    }

    public CompletableFuture<Boolean> backup() {
        CompletableFuture<Boolean> output = new CompletableFuture<>();
        try {
            mCompressor.prepare(mConfig);
            mStorage.prepare(mConfig);
            output = compress()
                .thenCompose(this::store)
                .thenCompose(this::applyRotation);
            output.whenComplete(this::closeStorage);
        } catch (Throwable ex) {
            ex.printStackTrace();
            output.completeExceptionally(ex);
        }
        return output;
    }

    private void closeStorage(Boolean aBoolean, Throwable throwable) {
        mStorage.close();
    }

    private CompletableFuture<File> compress() {
        notifySender("Compressing...");
        return mCompressor.compress();
    }

    private CompletableFuture<Boolean> store(File file) {
        notifySender("Uploading...");
        return mStorage.create(file);
    }
    private CompletableFuture<Boolean> delete(RemoteFile file) {
        if (file == null) return CompletableFuture.completedFuture(true);
        notifySender(String.format("Deleting %s...", file.getName()));
        return mStorage.delete(file);
    }

    private CompletableFuture<Boolean> applyRotation(Boolean stored) {
        if (!stored) return CompletableFuture.completedFuture(false);
        notifySender("Checking file rotation...");
        return mStorage.list()
                .thenApply(this::getOlderFile)
                .thenCompose(this::delete);
    }

    private RemoteFile getOlderFile(List<RemoteFile> remoteFiles) {
        if (remoteFiles.size() <= mConfig.getRotation()) {
            return null;
        }
        final Optional<RemoteFile> last = remoteFiles.stream().max((a, b) -> {
            boolean isOlder = a.getCreatedAt().isBefore(b.getCreatedAt());
            boolean isYounger = a.getCreatedAt().isBefore(b.getCreatedAt());
            if (isOlder) return 1;
            if (isYounger) return -1;
            return 0;
        });
        return last.orElse(null);
    }
}
