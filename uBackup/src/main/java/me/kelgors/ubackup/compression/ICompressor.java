package me.kelgors.ubackup.compression;

import me.kelgors.ubackup.configuration.BackupConfiguration;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public interface ICompressor {

    void prepare(BackupConfiguration config);
    CompletableFuture<File> compress();
}
