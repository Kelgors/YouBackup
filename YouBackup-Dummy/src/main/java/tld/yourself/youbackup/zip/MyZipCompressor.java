// MyZipCompressor.java
package tld.yourself.youbackup.zip;

import me.kelgors.youbackup.api.compression.Compressor;
import me.kelgors.youbackup.api.configuration.IBackupProfile;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MyZipCompressor extends Compressor {

    public static final String COMPRESSOR_TYPE = "zip";

    private Plugin mPlugin;

    // Will be used by YouBackup
    public MyZipCompressor(Plugin plugin) {
        // store it to log, runAsyncTask, ...
        mPlugin = plugin;
    }

    @Override
    public void prepare(IBackupProfile config) {
        // parse the needed configuration here
    }

    @Override
    public CompletableFuture<File> compress(List<File> inputFileList, File outputFile) {
        final CompletableFuture<File> output = new CompletableFuture<>();
        // create an async task
        mPlugin.getServer().getScheduler().runTaskAsynchronously(mPlugin, () -> {
            try {
                // do your compression here...
                // All is ok, complete the future with the output file
                output.complete(outputFile);
            } catch (IOException ex) {
                // Oh, theres something wrong, complete is with the exception
                output.completeExceptionally(ex);
            }
        });
        return output;
    }
}
