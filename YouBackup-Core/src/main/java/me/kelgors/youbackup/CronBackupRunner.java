package me.kelgors.youbackup;

import me.kelgors.youbackup.configuration.BackupConfiguration;
import me.kelgors.youbackup.configuration.Configuration;

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
        final Configuration config = mPlugin.getConfiguration();
        final ZonedDateTime now = ZonedDateTime.now();
        CompletableFuture<Boolean> future = CompletableFuture.completedFuture(true);
        for (final BackupConfiguration backup : config.getConfigurations()) {
            if (backup.isEnabled() && backup.getNextExecutionTime().isBefore(now)) {
                future = future.thenCompose((b) -> mPlugin.save(backup.getName()).whenComplete(new OnSaveComplete(mPlugin, backup)));
                backup.resetNextExecutionTime();
            }
        }
        mPlugin.startCron();
    }

    private static class OnSaveComplete implements BiConsumer<Boolean, Throwable> {

        private final YouBackupPlugin plugin;
        private final BackupConfiguration backup;

        OnSaveComplete(YouBackupPlugin plugin, BackupConfiguration backup) {
            this.plugin = plugin;
            this.backup = backup;
        }
        @Override
        public void accept(Boolean aBoolean, Throwable throwable) {
            plugin.getLogger().info(String.format("Task(backup: %s) has been %s", backup.getName(), aBoolean ? "completed" : "failed"));
            if (throwable != null) throwable.printStackTrace();
        }
    }
}
