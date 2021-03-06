package me.kelgors.youbackup;

import me.kelgors.youbackup.configuration.BackupProfile;
import me.kelgors.youbackup.configuration.PluginConfiguration;

import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class CronBackupRunner implements Runnable {

    private final YouBackupPlugin mPlugin;

    public CronBackupRunner(YouBackupPlugin plugin) {
        mPlugin = plugin;
    }

    @Override
    public void run() {
        mPlugin.getLogger().info("Start running task");
        boolean isRunning = false;
        final PluginConfiguration config = mPlugin.getConfiguration();
        final ZonedDateTime now = ZonedDateTime.now();
        CompletableFuture<Boolean> future = CompletableFuture.completedFuture(true);
        for (final BackupProfile backup : config.getProfiles()) {
            if (backup.isEnabled() && backup.getNextExecutionTime().isBefore(now)) {
                future = future.thenCompose((b) -> mPlugin.save(backup.getName()).whenComplete(new OnSaveComplete(mPlugin, backup)));
                backup.resetNextExecutionTime();
                isRunning = true;
            }
        }
        if (!isRunning) {
            mPlugin.getLogger().warning("No tasks have been launched");
        }
        mPlugin.startCron();
    }

    private static class OnSaveComplete implements BiConsumer<Boolean, Throwable> {

        private final YouBackupPlugin plugin;
        private final BackupProfile backup;

        OnSaveComplete(YouBackupPlugin plugin, BackupProfile backup) {
            this.plugin = plugin;
            this.backup = backup;
        }
        @Override
        public void accept(Boolean aBoolean, Throwable throwable) {
            plugin.getLogger().info(String.format("Task(profile: %s) has been %s", backup.getName(), aBoolean ? "completed" : "failed"));
            if (throwable != null) throwable.printStackTrace();
        }
    }
}
