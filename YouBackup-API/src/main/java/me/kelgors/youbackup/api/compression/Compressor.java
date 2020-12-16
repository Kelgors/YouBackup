package me.kelgors.youbackup.api.compression;

import me.kelgors.youbackup.api.AsyncOperation;
import org.bukkit.plugin.Plugin;

public abstract class Compressor extends AsyncOperation implements ICompressor {
    public Compressor(Plugin plugin) {
        super(plugin);
    }
}
