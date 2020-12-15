package me.kelgors.youbackup.extension;

import me.kelgors.youbackup.api.YouBackup;
import me.kelgors.youbackup.api.compression.ICompressor;
import me.kelgors.youbackup.api.storage.IStorage;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class YouBackupManager implements YouBackup {

    private final List<RegisteredExtension<IStorage>> mStorageExtensions = new ArrayList<>();
    private final List<RegisteredExtension<ICompressor>> mCompressorExtensions = new ArrayList<>();
    private final String mVersion;

    public YouBackupManager(String version) {
        mVersion = version;
    }

    public void clear() {
        mStorageExtensions.clear();
        mCompressorExtensions.clear();
    }

    @Override
    public String getVersion() {
        return mVersion;
    }

    @Override
    public void registerStorage(String type, Class<? extends IStorage> storage, Plugin plugin) {
        mStorageExtensions.add(new RegisteredExtension<>(type, storage, plugin));
    }

    @Override
    public void registerCompression(String type, Class<? extends ICompressor> compressor, Plugin plugin) {
        mCompressorExtensions.add(new RegisteredExtension<>(type, compressor, plugin));
    }

    @Override
    public void unregisterStorage(String type, Plugin plugin) {
        for (RegisteredExtension<IStorage> ext : mStorageExtensions) {
            if (plugin == ext.getPlugin() && type.equals(ext.getType())) {
                mStorageExtensions.remove(ext);
                break;
            }
        }
    }

    @Override
    public void unregisterCompression(String type, Plugin plugin) {
        for (RegisteredExtension<ICompressor> ext : mCompressorExtensions) {
            if (plugin == ext.getPlugin() && type.equals(ext.getType())) {
                mCompressorExtensions.remove(ext);
                break;
            }
        }
    }

    public RegisteredExtension<IStorage> getRegisteredStorage(String type) {
        for (int i = mStorageExtensions.size() - 1; i > -1; i--) {
            if (type.equals(mStorageExtensions.get(i).getType())) {
                return mStorageExtensions.get(i);
            }
        }
        return null;
    }

    public RegisteredExtension<ICompressor> getRegisteredCompressor(String type) {
        for (int i = mCompressorExtensions.size() - 1; i > -1; i--) {
            if (type.equals(mCompressorExtensions.get(i).getType())) {
                return mCompressorExtensions.get(i);
            }
        }
        return null;
    }

}
