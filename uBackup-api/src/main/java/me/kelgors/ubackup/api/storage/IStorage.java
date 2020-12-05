package me.kelgors.ubackup.api.storage;

import me.kelgors.ubackup.api.configuration.IBackupConfiguration;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IStorage {
    String getStorageType();

    void prepare(IBackupConfiguration config);

    /**
     * List files on destination
     * Used to rotate files
     * @return
     */
    CompletableFuture<List<IRemoteFile>> list();

    /**
     * create a file on destination
     * @param file
     * @return
     */
    CompletableFuture<Boolean> create(File file);

    /**
     * Delete the specified remoteFile on destination
     * Used to rotate files
     * @param remoteFile
     * @return
     */
    CompletableFuture<Boolean> delete(IRemoteFile remoteFile);

    /**
     * Close connection if there is one
     * @return
     */
    CompletableFuture<Boolean> close();
}
