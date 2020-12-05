package me.kelgors.youbackup.api;

import me.kelgors.youbackup.api.compression.ICompressor;
import me.kelgors.youbackup.api.storage.IStorage;

public interface YouBackup {
    void setStorage(String type, Class<? extends IStorage> storage);
    void setCompression(String type, Class<? extends ICompressor> compressor);
    void removeStorage(String type);
    void removeCompression(String type);
}
