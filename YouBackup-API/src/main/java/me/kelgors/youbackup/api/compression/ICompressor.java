package me.kelgors.youbackup.api.compression;

import me.kelgors.youbackup.api.configuration.IBackupProfile;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ICompressor {
    /**
     * This is where you get configuration parameters if needed
     * @param profile profile information
     */
    void prepare(IBackupProfile profile);

    /**
     * This is where you transform a list of files to a compressed file
     * @param inputFileList Files to be compressed
     * @param outputFile The compressed file
     * @return The compressed file
     */
    CompletableFuture<File> compress(List<File> inputFileList, File outputFile);
}
