package me.kelgors.ubackup.storage;

import me.kelgors.ubackup.WorldConfiguration;
import me.kelgors.ubackup.configuration.BackupConfiguration;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public interface IStorage {
    String getStorageType();

    void prepare(BackupConfiguration config);
    CompletableFuture<Boolean> backup(File file);
}
