package me.kelgors.youbackup.compression;

import me.kelgors.youbackup.api.compression.Compressor;
import me.kelgors.youbackup.api.configuration.IBackupProfile;
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

public class ZipCompressor extends Compressor {

    private final Logger mLogger;

    String mFilename;

    public ZipCompressor(Plugin plugin) {
        super(plugin);
        mLogger = plugin.getLogger();
    }

    @Override
    public void prepare(IBackupProfile config) {
        mFilename = config.getFilename();
        if (mFilename == null || "".equals(mFilename.trim())) {
            // empty filename
        }
    }

    @Override
    public CompletableFuture<File> compress(List<File> files, File outputFile) {
        final CompletableFuture<File> output = new CompletableFuture<>();
        runTaskAsync(() -> {
            mLogger.info("Begin zipping");
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
                mLogger.info("Zipping done");
            } catch (IOException e) {
                e.printStackTrace();
                output.completeExceptionally(e);
            } finally {
                // closing zip stream
                try {
                    if (zout != null) zout.close();
                } catch (IOException ioex) {
                    mLogger.warning("Cannot close the zip stream for: " + outputFile.getAbsolutePath());
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
        mLogger.finer(String.format("ZipFile(%s)", item.getName()));
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
        String relativeFilename = getRelativeFileName(file);
        // add directory
        stream.putNextEntry(new ZipEntry( relativeFilename + (relativeFilename.endsWith("/") ? "" : "/")));
        stream.closeEntry();
    }

    public String getRelativeFileName(File file) throws IOException {
        final File serverPath = new File(".");
        return file.getAbsolutePath().replace(serverPath.getCanonicalPath(), "");
    }

}
