// MyStorage.java
package tld.yourself.youbackup.zip;

import me.kelgors.youbackup.api.configuration.IBackupProfile;
import me.kelgors.youbackup.api.storage.BasicRemoteFile;
import me.kelgors.youbackup.api.storage.IRemoteFile;
import me.kelgors.youbackup.api.storage.Storage;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MyStorage extends Storage {
    public static final String STORAGE_TYPE = "gdrive";
    private final Plugin mPlugin;
    private String mHostname;

    public MyStorage(Plugin plugin) {
        mPlugin = plugin;
    }

    @Override
    public String getStorageType() {
        return STORAGE_TYPE;
    }

    @Override
    public void prepare(IBackupProfile config) {
        mHostname = config.getDestination().getString("host");
    }

    @Override
    public CompletableFuture<List<IRemoteFile>> list() {
        final CompletableFuture<List<IRemoteFile>> output = new CompletableFuture<>();
        mPlugin.getServer().getScheduler().runTaskAsynchronously(mPlugin, () -> {
            try {
                List<Object> files = getFileFromRemoteService(); // list your files from remote service
                // transform files from service to IRemoteFile
                // The second parameter is mandatory for rotation
                output.complete(files.stream().map((o) -> new BasicRemoteFile(o.name, o.createdAt)).collect(Collectors.toList()));
            } catch (Exception ex) {
                output.completeExceptionally(ex);
            }
        });
        return output;
    }

    @Override
    public CompletableFuture<Boolean> create(File file) {
        final CompletableFuture<Boolean> output = new CompletableFuture<>();
        mPlugin.getServer().getScheduler().runTaskAsynchronously(mPlugin, () -> {
            try {
                boolean result = yourUploadMethod(file); // list your files from remote service
                output.complete(result);
            } catch (Exception ex) {
                output.completeExceptionally(ex);
            }
        });
        return output;
    }

    @Override
    public CompletableFuture<Boolean> delete(IRemoteFile remoteFile) {
        final CompletableFuture<Boolean> output = new CompletableFuture<>();
        mPlugin.getServer().getScheduler().runTaskAsynchronously(mPlugin, () -> {
            try {
                boolean result = deleteYourFileRemotely(file);
                output.complete(result);
            } catch (Exception ex) {
                output.completeExceptionally(ex);
            }
        });
        return output;
    }

    @Override
    public CompletableFuture<Boolean> close() {
        final CompletableFuture<Boolean> output = new CompletableFuture<>();
        mPlugin.getServer().getScheduler().runTaskAsynchronously(mPlugin, () -> {
            try {
                myServiceClient.closeConnection();
                output.complete(true);
            } catch (Exception ex) {
                output.completeExceptionally(ex);
            }

        });
        return output;
    }
}
