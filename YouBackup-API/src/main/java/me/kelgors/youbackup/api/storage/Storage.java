package me.kelgors.youbackup.api.storage;

import me.kelgors.youbackup.api.AsyncOperation;
import org.bukkit.plugin.Plugin;

public abstract class Storage extends AsyncOperation implements IStorage {
    public Storage(Plugin plugin) {
        super(plugin);
    }
}
