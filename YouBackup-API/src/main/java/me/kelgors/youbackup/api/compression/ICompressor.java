package me.kelgors.youbackup.api.compression;

import me.kelgors.youbackup.api.configuration.IBackupConfiguration;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public interface ICompressor {
    void prepare(IBackupConfiguration config);
    CompletableFuture<File> compress();
}
