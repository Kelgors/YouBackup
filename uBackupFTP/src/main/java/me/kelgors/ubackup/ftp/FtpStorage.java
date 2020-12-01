package me.kelgors.ubackup.ftp;

import me.kelgors.ubackup.WorldConfiguration;
import me.kelgors.ubackup.storage.IStorage;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class FtpStorage implements IStorage {
    public static final String STORAGE_TYPE = "ftp";

    @Override
    public String getStorageType() {
        return STORAGE_TYPE;
    }

    @Override
    public void prepare(WorldConfiguration config) {

    }

    @Override
    public CompletableFuture<Boolean> backup(File file) {
        return null;
    }
}
