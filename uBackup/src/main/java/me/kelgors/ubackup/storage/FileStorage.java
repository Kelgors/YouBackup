package me.kelgors.ubackup.storage;

import me.kelgors.ubackup.api.configuration.IBackupConfiguration;
import me.kelgors.ubackup.api.storage.BasicRemoteFile;
import me.kelgors.ubackup.api.storage.IRemoteFile;
import me.kelgors.ubackup.api.storage.IStorage;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class FileStorage implements IStorage {
    private File mDestinationFile;
    private final Plugin mPlugin;

    public FileStorage(Plugin plugin) {
        mPlugin = plugin;
    }

    @Override
    public String getStorageType() {
        return "file";
    }

    @Override
    public void prepare(IBackupConfiguration config) {
        String path = config.getDestination().getString("path", ".");
        mDestinationFile = new File(path);
    }

    @Override
    public CompletableFuture<Boolean> create(File file) {
        // save is in server directory, so do nothing
        final CompletableFuture<Boolean> output = new CompletableFuture<>();
        final Logger logger = mPlugin.getLogger();

        mPlugin.getServer().getScheduler().runTaskAsynchronously(mPlugin, () -> {
            try {
                if (!mDestinationFile.exists() && !mDestinationFile.mkdirs()) {
                    logger.warning(String.format("[File] Cannot create directory %s", mDestinationFile.getAbsolutePath()));
                }
                final Path sourcePath = Paths.get(file.getAbsolutePath());
                final Path destPath = Paths.get(mDestinationFile.getAbsolutePath(), file.getName());
                logger.info(String.format("[File] Move %s to %s", sourcePath.toString(), destPath.toString()));
                Files.move(sourcePath, destPath);
                output.complete(true);
            } catch (IOException e) {
                e.printStackTrace();
                output.completeExceptionally(e);
            }
        });

        return output;
    }

    @Override
    public CompletableFuture<List<IRemoteFile>> list() {
        final CompletableFuture<List<IRemoteFile>> output = new CompletableFuture<>();
        mPlugin.getServer().getScheduler().runTaskAsynchronously(mPlugin, () -> {
            final ArrayList<IRemoteFile> remoteFiles = new ArrayList<>();
            for (File file : Objects.requireNonNull(mDestinationFile.listFiles())) {
                try {
                    BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                    remoteFiles.add(new BasicRemoteFile(file.getName(), attributes.creationTime().toInstant().atZone(ZoneId.systemDefault())));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            output.complete(remoteFiles);
        });
        return output;
    }

    @Override
    public CompletableFuture<Boolean> delete(IRemoteFile remoteFile) {
        final CompletableFuture<Boolean> output = new CompletableFuture<>();
        mPlugin.getServer().getScheduler().runTaskAsynchronously(mPlugin, () -> {
            final File file = new File(mDestinationFile, remoteFile.getName());
            if (file.exists()) {
                output.complete(file.delete());
            } else {
                output.complete(false);
            }
        });
        return output;
    }

    @Override
    public CompletableFuture<Boolean> close() {
        return CompletableFuture.completedFuture(true);
    }
}
