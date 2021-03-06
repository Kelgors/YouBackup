package me.kelgors.youbackup.api.storage;

import me.kelgors.youbackup.api.configuration.IBackupProfile;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IStorage {
    String getStorageType();

    /**
     * This is where you get configuration parameters if needed
     * @param profile profile information
     */
    void prepare(IBackupProfile profile);

    /**
     * List files on destination
     * Used to rotate files
     * @return
     */
    CompletableFuture<List<IRemoteFile>> list();

    /**
     * Create a file on destination
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
