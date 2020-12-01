package me.kelgors.ubackup;

import me.kelgors.ubackup.compression.ICompressor;
import me.kelgors.ubackup.storage.IStorage;
import org.bukkit.World;
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
    World mWorld;
    WorldConfiguration mWorldConfig;

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

    public BackupBuilder setWorld(World world, WorldConfiguration config) {
        mWorld = world;
        mWorldConfig = config;
        return this;
    }

    private void notifySender(String message) {
        mPlugin.getLogger().info(uBackupPlugin.TAG + message);
        if (mCommandSender == null || !(mCommandSender instanceof Player)) return;
        mCommandSender.sendMessage(uBackupPlugin.TAG + message);
    }

    public CompletableFuture<Boolean> backup() {
        final CompletableFuture<World> future = CompletableFuture.completedFuture(mWorld);
        CompletableFuture<Boolean> output = CompletableFuture.completedFuture(false);
        try {
            output = future.thenCompose(this::compress).thenCompose(this::store);
        } catch (Throwable ex) {
            ex.printStackTrace();
            future.completeExceptionally(ex);
        }
        return output;
    }

    private CompletableFuture<File> compress(World world) {
        notifySender(String.format("Compressing world %s...", world.getName()));
        mCompressor.prepare(mWorldConfig);
        return mCompressor.compress(world);
    }

    private CompletableFuture<Boolean> store(File file) {
        notifySender(String.format("Saving world %s...", mWorld.getName()));
        mStorage.prepare(mWorldConfig);
        return mStorage.backup(file);
    }
}
