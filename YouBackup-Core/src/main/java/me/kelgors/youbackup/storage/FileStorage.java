package me.kelgors.youbackup.storage;

import me.kelgors.youbackup.api.configuration.IBackupProfile;
import me.kelgors.youbackup.api.storage.BasicRemoteFile;
import me.kelgors.youbackup.api.storage.IRemoteFile;
import me.kelgors.youbackup.api.storage.Storage;
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

public class FileStorage extends Storage {
    private final Logger mLogger;
    private File mDestinationFile;

    public FileStorage(Plugin plugin) {
        super(plugin);
        mLogger = plugin.getLogger();
    }

    @Override
    public String getStorageType() {
        return "file";
    }

    @Override
    public void prepare(IBackupProfile config) {
        String path = config.getDestination().getString("path", ".");
        mDestinationFile = new File(path);
    }

    @Override
    public CompletableFuture<Boolean> create(File file) {
        // save is in server directory, so do nothing
        final CompletableFuture<Boolean> output = new CompletableFuture<>();

        runTaskAsync(() -> {
            try {
                if (!mDestinationFile.exists() && !mDestinationFile.mkdirs()) {
                    mLogger.warning(String.format("[File] Cannot create directory %s", mDestinationFile.getAbsolutePath()));
                }
                final Path sourcePath = Paths.get(file.getAbsolutePath());
                final Path destPath = Paths.get(mDestinationFile.getAbsolutePath(), file.getName());
                mLogger.info(String.format("[File] Move %s to %s", sourcePath.toString(), destPath.toString()));
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
        runTaskAsync(() -> {
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
        runTaskAsync(() -> {
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
