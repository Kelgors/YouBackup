package me.kelgors.youbackup.extension;

import me.kelgors.youbackup.api.compression.Compressor;
import me.kelgors.youbackup.api.compression.ICompressor;
import me.kelgors.youbackup.api.storage.IStorage;
import me.kelgors.youbackup.api.storage.Storage;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class InstantiationUtils {
    public static IStorage instantiateStorage(Class<? extends IStorage> klass, Plugin plugin) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        for (Constructor<?> ctr : klass.getConstructors()) {
            Class<?>[] args = ctr.getParameterTypes();
            if (args.length == 1 && args[0].equals(Plugin.class)) {
                return (Storage) ctr.newInstance(plugin);
            }
        }
        return null;
    }
    public static ICompressor instantiateCompressor(Class<? extends ICompressor> klass, Plugin plugin) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        for (Constructor<?> ctr : klass.getConstructors()) {
            Class<?>[] args = ctr.getParameterTypes();
            if (args.length == 1 && args[0].equals(Plugin.class)) {
                return (Compressor) ctr.newInstance(plugin);
            }
        }
        return null;
    }
}
