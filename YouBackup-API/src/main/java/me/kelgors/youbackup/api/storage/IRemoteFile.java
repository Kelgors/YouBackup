package me.kelgors.youbackup.api.storage;

import java.time.ZonedDateTime;

/**
 * Simple representation of file on remote service
 */
public interface IRemoteFile {
    /**
     * The remote file name to identify it quickly
     * for ftp, it would be the complete path to the file
     * fot s3, it would be the key
     * for File, it would be the File::getAbsolutePath()
     * @return
     */
    String getName();

    /**
     * The creation date of the file stored on the remote service
     * @return
     */
    ZonedDateTime getCreatedAt();
}
