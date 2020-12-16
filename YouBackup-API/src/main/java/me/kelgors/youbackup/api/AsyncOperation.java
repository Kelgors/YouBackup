package me.kelgors.youbackup.api;

import org.bukkit.plugin.Plugin;

public class AsyncOperation {
    private final Plugin mPlugin;

    public AsyncOperation(Plugin plugin) {
        mPlugin = plugin;
    }

    protected Plugin getPlugin() {
        return mPlugin;
    }

    /**
     * Alias for Plugin::getServer::getScheduler::runTaskAsynchronously(Plugin, Runnable)
     * @param runnable
     */
    protected void runTaskAsync(Runnable runnable) {
        mPlugin.getServer().getScheduler().runTaskAsynchronously(mPlugin, runnable);
    }

    /**
     * Alias for Plugin::getServer::getScheduler::runTask(Plugin, Runnable)
     * @param runnable
     */
    protected void runTask(Runnable runnable) {
        mPlugin.getServer().getScheduler().runTask(mPlugin, runnable);
    }
}
