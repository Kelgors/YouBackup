package me.kelgors.ubackup.compression;

import me.kelgors.ubackup.FilenameFormatter;
import me.kelgors.ubackup.WorldConfiguration;
import me.kelgors.ubackup.uBackupPlugin;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipCompressor implements ICompressor {

    final Plugin mPlugin;
    private WorldConfiguration mWorldConfiguration;

    public ZipCompressor(Plugin plugin) {
        mPlugin = plugin;
    }

    @Override
    public void prepare(WorldConfiguration configuration) {
        mWorldConfiguration = configuration;
    }

    @Override
    public CompletableFuture<File> compress(World world) {
        mPlugin.getLogger().info("Disable autosave for world: " + world.getName());
        world.setAutoSave(false);
        final CompletableFuture<File> output = saveWorld(world).thenCompose(this::zipWorld);
        output.whenComplete((f, t) -> {
            mPlugin.getLogger().info("Enable autosave back for world: " + world.getName());
            world.setAutoSave(true);
        });
        return output;
    }

    private CompletableFuture<World> saveWorld(World world) {
        final CompletableFuture<World> output = new CompletableFuture<>();
        final Logger logger = mPlugin.getLogger();
        final Server server = mPlugin.getServer();
        server.broadcastMessage(uBackupPlugin.SERVER_TAG + "Saving world...");
        server.getScheduler().runTaskLater(mPlugin, () -> {
            try {
                logger.info("Saving world: " + world.getName());
                world.save();
                server.broadcastMessage(uBackupPlugin.SERVER_TAG + "World saved.");
                output.complete(world);
            } catch (Exception ex) {
                output.completeExceptionally(ex);
            }
        }, 50);
        return output;
    }

    private CompletableFuture<File> zipWorld(World world) {
        final CompletableFuture<File> output = new CompletableFuture<>();
        mPlugin.getServer().getScheduler().runTaskAsynchronously(mPlugin, () -> {
            final Logger logger = mPlugin.getLogger();
            final File folder = world.getWorldFolder();
            logger.info(String.format("Backup world %s", world.getName()));
            logger.info(String.format("Begin zipping %s", world.getName()));
            // preparing file
            final String uuid = UUID.randomUUID().toString();
            final String filename = FilenameFormatter.format(mWorldConfiguration.filename, LocalDateTime.now(), world);
            final File tmpFile = new File(filename);
            ZipOutputStream zout = null;
            try {
                // try creating it
                if (!tmpFile.createNewFile()) {
                    logger.severe("Cannot create file in : " + tmpFile.getAbsolutePath());
                }
                // preparing zipping
                final FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);
                zout = new ZipOutputStream(fileOutputStream);
                // zip world folder
                zipDirectory(folder, folder.getName(), zout);
                logger.info(String.format("Zipping done %s", world.getName()));
            } catch (IOException ex) {
                output.completeExceptionally(ex);
            } finally {
                // closing zip stream
                try {
                    if (zout != null) zout.close();
                } catch (IOException ioex) {
                    logger.warning("Cannot close the zip stream for: " + tmpFile.getAbsolutePath());
                    if (!output.isCompletedExceptionally()) {
                        output.completeExceptionally(ioex);
                    }
                }
                // complete future
                if (!output.isCompletedExceptionally()) {
                    output.complete(tmpFile);
                }
            }
        });
        return output;
    }

    public void zipDirectory(File file, String filename, ZipOutputStream stream) throws IOException {
        File[] files = file.listFiles();
        for (File item : files) {
            String relativeFilename = String.format("%s/%s", filename, item.getName());
            if (item.isDirectory()) {
                // add directory
                stream.putNextEntry(new ZipEntry(relativeFilename + (relativeFilename.endsWith("/") ? "" : "/")));
                stream.closeEntry();
                // zip directory content
                zipDirectory(item, relativeFilename, stream);
                continue;
            }
            byte[] buffer = new byte[1024];
            FileInputStream fin = new FileInputStream(item);
            stream.putNextEntry(new ZipEntry(relativeFilename));
            int length;
            while ((length = fin.read(buffer)) > 0) {
                stream.write(buffer, 0, length);
            }
            stream.closeEntry();
        }
    }
}
