package me.kelgors.ubackup.api;

import me.kelgors.ubackup.api.compression.ICompressor;
import me.kelgors.ubackup.api.storage.IStorage;

public interface YouBackup {
    void setStorage(String type, Class<? extends IStorage> storage);
    void setCompression(String type, Class<? extends ICompressor> compressor);
    void removeStorage(String type);
    void removeCompression(String type);
}
