package me.kelgors.youbackup.extension;

import org.bukkit.plugin.Plugin;

public class RegisteredExtension<T> {
    private final String mType;
    private final Class<? extends T> mExtension;
    private final Plugin mPlugin;

    public RegisteredExtension(String type, Class<? extends T> extension, Plugin plugin) {
        mType = type;
        mExtension = extension;
        mPlugin = plugin;
    }

    public Plugin getPlugin() {
        return mPlugin;
    }

    public Class<? extends T> getExtension() {
        return mExtension;
    }

    public String getType() {
        return mType;
    }
}