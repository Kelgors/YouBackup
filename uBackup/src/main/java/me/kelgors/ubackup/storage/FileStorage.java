package me.kelgors.ubackup.storage;

import me.kelgors.ubackup.WorldConfiguration;
import me.kelgors.ubackup.configuration.BackupConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    public void prepare(BackupConfiguration config) {
        String path = config.destination.getString("path", ".");
        mDestinationFile = new File(path);
    }

    @Override
    public CompletableFuture<Boolean> backup(File file) {
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
}
