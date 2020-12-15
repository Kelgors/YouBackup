package me.kelgors.youbackup.compression;

import me.kelgors.youbackup.api.compression.ICompressor;
import me.kelgors.youbackup.api.configuration.IBackupConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipCompressor implements ICompressor {

    final Plugin mPlugin;

    String mFilename;
//    List<String> mWorlds;
//    List<File> mIncludes;
//    List<File> mExcludes;
//    String mProfileName;

    public ZipCompressor(Plugin plugin) {
        mPlugin = plugin;
    }

    @Override
    public void prepare(IBackupConfiguration config) {
//        mProfileName = config.getName();
        mFilename = config.getFilename();
        if (mFilename == null || "".equals(mFilename.trim())) {
            // empty filename
        }
    }

    @Override
    public CompletableFuture<File> compress(List<File> files, File outputFile) {
        final CompletableFuture<File> output = new CompletableFuture<>();
        mPlugin.getServer().getScheduler().runTaskAsynchronously(mPlugin, () -> {
            final Logger logger = mPlugin.getLogger();
            logger.info("Begin zipping");
            ZipOutputStream zout = null;
            try {
                // preparing zipping
                final FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
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
            } catch (IOException e) {
                e.printStackTrace();
                output.completeExceptionally(e);
            } finally {
                // closing zip stream
                try {
                    if (zout != null) zout.close();
                } catch (IOException ioex) {
                    logger.warning("Cannot close the zip stream for: " + outputFile.getAbsolutePath());
                    if (!output.isCompletedExceptionally()) {
                        output.completeExceptionally(ioex);
                    }
                }
                // complete future
                if (!output.isCompletedExceptionally()) {
                    output.complete(outputFile);
                }
            }
        });
        return output;
    }

    void zipFile(File item, String filename, ZipOutputStream stream) throws IOException {
        mPlugin.getLogger().finer(String.format("ZipFile(%s)", item.getName()));
        final String relativeFilename = getRelativeFileName(item); // String.format("%s/%s", filename, item.getName());
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
        String relativeFilename = getRelativeFileName(file); // String.format("%s/%s", filename, file.getName());
        // add directory
        stream.putNextEntry(new ZipEntry( relativeFilename + (relativeFilename.endsWith("/") ? "" : "/")));
        stream.closeEntry();
    }

    public String getRelativeFileName(File file) throws IOException {
        final File serverpath = new File(".");
        return file.getAbsolutePath().replace(serverpath.getCanonicalPath(), "");
    }

//    public void zipDirectory(File file, String filename, ZipOutputStream stream) throws IOException {
//        mPlugin.getLogger().finer(String.format("ZipDirectory(%s)", file.getName()));
//        File[] files = file.listFiles();
//        for (File item : files) {
//            String relativeFilename = String.format("%s/%s", filename, item.getName());
//            if (isExcluded(item)) {
//                mPlugin.getLogger().finer(String.format("Ignore(%s)", file.getName()));
//                continue;
//            }
//            if (item.isDirectory()) {
//                // add directory
//                stream.putNextEntry(new ZipEntry(relativeFilename + (relativeFilename.endsWith("/") ? "" : "/")));
//                stream.closeEntry();
//                // zip directory content
//                zipDirectory(item, relativeFilename, stream);
//            } else {
//                zipFile(item, filename, stream);
//            }
//        }
//    }

//    boolean isExcluded(File file) throws IOException {
//        for (File excludedFile : mExcludes) {
//            if (file.getCanonicalPath().equals(excludedFile.getCanonicalPath())) {
//                return true;
//            }
//        }
//        return false;
//    }
}
