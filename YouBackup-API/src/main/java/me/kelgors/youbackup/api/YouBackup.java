package me.kelgors.youbackup.api;

import me.kelgors.youbackup.api.compression.ICompressor;
import me.kelgors.youbackup.api.storage.IStorage;
import org.bukkit.plugin.Plugin;

public interface YouBackup {
    String getVersion();
    void registerStorage(String type, Class<? extends IStorage> storage, Plugin plugin);
    void registerCompression(String type, Class<? extends ICompressor> compressor, Plugin plugin);
    void unregisterStorage(String type, Plugin plugin);
    void unregisterCompression(String type, Plugin plugin);
}
