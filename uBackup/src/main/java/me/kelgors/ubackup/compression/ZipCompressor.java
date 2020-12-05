package me.kelgors.ubackup.compression;

import me.kelgors.ubackup.FilenameFormatter;
import me.kelgors.ubackup.api.compression.ICompressor;
import me.kelgors.ubackup.api.configuration.IBackupConfiguration;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipCompressor implements ICompressor {

    final Plugin mPlugin;

    String mFilename;
    List<String> mWorlds;
    List<File> mIncludes;
    List<File> mExcludes;
    String mProfileName;

    public ZipCompressor(Plugin plugin) {
        mPlugin = plugin;
    }

    @Override
    public void prepare(IBackupConfiguration config) {
        mProfileName = config.getName();
        mFilename = config.getFilename();
        final ConfigurationSection compression = config.getCompression();
        mIncludes = compression.getList("include", new ArrayList<>()).stream()
                .map(item -> new File((String) item))
                .collect(Collectors.toList());
        mExcludes = compression.getList("exclude", new ArrayList<>()).stream()
                .map(item -> new File((String) item))
                .collect(Collectors.toList());
        mWorlds = compression.getList("worlds", new ArrayList<>()).stream()
                .map(item -> (String) item)
                .collect(Collectors.toList());
        if (mFilename == null || "".equals(mFilename.trim())) {
            // empty filename
        }
        if (mIncludes.size() + mWorlds.size() == 0) {
            // empty zip
        }
    }

    @Override
    public CompletableFuture<File> compress() {
        // save worlds && get world folders
        final List<File> files;
        final CompletableFuture<File> output = saveWorlds()
                .thenApply((list) -> {
                    // add path in include property
                    list.addAll(mIncludes);
                    return list;
                })
                .thenCompose(this::zipFiles);
        output.whenComplete(this::resetWorlds);
        return output;
    }

    private CompletableFuture<List<File>> saveWorlds() {
        final CompletableFuture<List<File>> output = new CompletableFuture<>();
        if (mWorlds.size() > 0) {
            saveWorldAtAsync(0, output, new ArrayList<>());
        } else {
            output.complete(new ArrayList<>());
        }
        return output;
    }

    private void saveWorldAtAsync(final int index, final CompletableFuture<List<File>> future, final List<File> worldDirectories) {
        mPlugin.getServer().getScheduler().runTaskLater(mPlugin, () -> {
            final Logger logger = mPlugin.getLogger();
            final String worldName = mWorlds.get(index);
            final World world = mPlugin.getServer().getWorld(worldName);
            if (world != null) {
                // disable save until files are added to zip file
                logger.info("Disable autosave for world: " + worldName);
                world.setAutoSave(false);
                // save world to get the latest world commits
                logger.info("Save world: " + worldName);
                world.save();
                worldDirectories.add(world.getWorldFolder());
            } else {
                logger.warning(String.format("Skipping %s, world not found", worldName));
            }
            if (index + 1 < mWorlds.size()) {
                saveWorldAtAsync(index + 1, future, worldDirectories);
            } else {
               future.complete(worldDirectories);
            }
        }, 20);
    }

    private void resetWorlds(File file, Throwable throwable) {
        final Logger logger = mPlugin.getLogger();
        for (String worldName : mWorlds) {
            World world = mPlugin.getServer().getWorld(worldName);
            if (world != null) {
                logger.info("Enable autosave back for world: " + worldName);
                world.setAutoSave(true);
            }
        }
    }

    private CompletableFuture<File> zipFiles(List<File> files) {
        final CompletableFuture<File> output = new CompletableFuture<>();
        mPlugin.getServer().getScheduler().runTaskAsynchronously(mPlugin, () -> {
            final Logger logger = mPlugin.getLogger();
            logger.info("Begin zipping");
            // preparing file
            final String filename = FilenameFormatter.format(mFilename, LocalDateTime.now(), mProfileName);
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
                // zip files
                for (File item : files) {
                    if (item.isDirectory()) {
                        zipDirectory(item, item.getName(), zout);
                    } else {
                        zipFile(item, item.getName(), zout);
                    }
                }
                logger.info("Zipping done");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                output.completeExceptionally(e);
            } catch (IOException e) {
                e.printStackTrace();
                output.completeExceptionally(e);
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

    void zipFile(File item, String filename, ZipOutputStream stream) throws IOException {
        if (isExcluded(item)) {
            mPlugin.getLogger().finer(String.format("Ignore(%s)", item.getName()));
            return;
        }
        mPlugin.getLogger().finer(String.format("ZipFile(%s)", item.getName()));
        final String relativeFilename = String.format("%s/%s", filename, item.getName());
        final byte[] buffer = new byte[1024];
        final FileInputStream fin = new FileInputStream(item);
        stream.putNextEntry(new ZipEntry(relativeFilename));
        int length;
        while ((length = fin.read(buffer)) > 0) {
            stream.write(buffer, 0, length);
        }
        stream.closeEntry();
    }

    public void zipDirectory(File file, String filename, ZipOutputStream stream) throws IOException {
        mPlugin.getLogger().finer(String.format("ZipDirectory(%s)", file.getName()));
        File[] files = file.listFiles();
        for (File item : files) {
            String relativeFilename = String.format("%s/%s", filename, item.getName());
            if (isExcluded(item)) {
                mPlugin.getLogger().finer(String.format("Ignore(%s)", file.getName()));
                continue;
            }
            if (item.isDirectory()) {
                // add directory
                stream.putNextEntry(new ZipEntry(relativeFilename + (relativeFilename.endsWith("/") ? "" : "/")));
                stream.closeEntry();
                // zip directory content
                zipDirectory(item, relativeFilename, stream);
            } else {
                zipFile(item, filename, stream);
            }
        }
    }

    boolean isExcluded(File file) throws IOException {
        for (File excludedFile : mExcludes) {
            if (file.getCanonicalPath().equals(excludedFile.getCanonicalPath())) {
                return true;
            }
        }
        return false;
    }
}
