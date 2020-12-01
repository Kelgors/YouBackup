package me.kelgors.ubackup.storage;

import me.kelgors.ubackup.WorldConfiguration;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public interface IStorage {
    String getStorageType();

    void prepare(WorldConfiguration config);
    CompletableFuture<Boolean> backup(File file);
}
