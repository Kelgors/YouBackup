package me.kelgors.ubackup.api.compression;

import me.kelgors.ubackup.api.configuration.IBackupConfiguration;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public interface ICompressor {
    void prepare(IBackupConfiguration config);
    CompletableFuture<File> compress();
}
