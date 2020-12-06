// MyZipCompressor.java
package tld.yourself.youbackup.zip;

import me.kelgors.youbackup.api.compression.ICompressor;
import me.kelgors.youbackup.api.configuration.IBackupConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MyZipCompressor implements ICompressor {

    public static final String COMPRESSOR_TYPE = "zip";

    private Plugin mPlugin;
    private List<String> mWorlds;

    // Will be used by YouBackup
    public MyZipCompressor(Plugin plugin) {
        // store it to log, runAsyncTask, ...
        mPlugin = plugin;
    }

    @Override
    public void prepare(IBackupConfiguration config) {
        // parse the needed configuration here
        mWorlds = config.getCompression().getList("worlds", new ArrayList<>())
            .stream().map((s) -> (String) s)
            .collect(Collectors.toList());
    }

    @Override
    public CompletableFuture<File> compress(File outputFile) {
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
