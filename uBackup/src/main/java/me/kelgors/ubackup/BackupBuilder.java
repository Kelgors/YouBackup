package me.kelgors.ubackup;

import me.kelgors.ubackup.compression.ICompressor;
import me.kelgors.ubackup.configuration.BackupConfiguration;
import me.kelgors.ubackup.storage.IStorage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
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
            output = compress().thenCompose(this::store);
        } catch (Throwable ex) {
            ex.printStackTrace();
            output.completeExceptionally(ex);
        }
        return output;
    }

    private CompletableFuture<File> compress() {
        notifySender("Compressing...");
        return mCompressor.compress();
    }

    private CompletableFuture<Boolean> store(File file) {
        notifySender("Uploading...");
        return mStorage.backup(file);
    }
}
