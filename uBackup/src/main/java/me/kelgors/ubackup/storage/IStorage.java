package me.kelgors.ubackup.storage;

import me.kelgors.ubackup.configuration.BackupConfiguration;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IStorage {
    String getStorageType();

    void prepare(BackupConfiguration config);

    /**
     * List files on destination
     * Used to rotate files
     * @return
     */
    CompletableFuture<List<RemoteFile>> list();

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
    CompletableFuture<Boolean> delete(RemoteFile remoteFile);

    /**
     * Close connection if there is one
     * @return
     */
    CompletableFuture<Boolean> close();

}
