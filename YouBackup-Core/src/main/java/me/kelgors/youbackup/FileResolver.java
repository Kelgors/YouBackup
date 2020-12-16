package me.kelgors.youbackup;

import me.kelgors.youbackup.api.configuration.IBackupProfile;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Prepare world files, inclusions and filter exclusions
 */
public class FileResolver {

    private final Plugin mPlugin;
    private final List<File> mIncludes;
    private final List<File> mExcludes;

    public FileResolver(Plugin plugin, IBackupProfile config) {
        mPlugin = plugin;
        mIncludes = config.getIncludes().stream()
            .map(File::new)
            .collect(Collectors.toList());
        mExcludes = config.getExclude().stream()
            .map(File::new)
            .collect(Collectors.toList());
        // add work folder to exclusion
        mExcludes.add(new File(plugin.getDataFolder() + File.separator + "work"));
    }

    public CompletableFuture<List<File>> resolve(List<File> worldFolders) {
        mPlugin.getLogger().info("Resolving files...");
        List<File> files = new ArrayList<>();
        files.addAll(worldFolders);
        files.addAll(mIncludes);
        return flattenFiles(files);
    }

    private CompletableFuture<List<File>> flattenFiles(final List<File> files) {
        final CompletableFuture<List<File>> output = new CompletableFuture<>();
        mPlugin.getServer().getScheduler().runTaskAsynchronously(mPlugin, () -> {
            final List<File> allFiles = files.stream()
                .map(this::getFilesDeeply)
                // flatten List of List of File
                .reduce(new ArrayList<>(), (acc, item) -> {
                    acc.addAll(item);
                    return acc;
                });
            mPlugin.getLogger().info(String.format("Resolved %d files.", allFiles.size()));
            output.complete(allFiles);
        });
        return output;
    }

    private List<File> getFilesDeeply(File file) {
        final List<File> output = new ArrayList<>();
        try {
            if (file.exists()) {
                // remove .. && .
                final File normalizedFile = file.toPath().normalize().toFile();
                output.add(normalizedFile);
                if (file.isDirectory()) {
                    return getFilesDeeply(normalizedFile, output);
                }
            } else {
                mPlugin.getLogger().warning(String.format("File(%s) does not exists", file.getAbsolutePath()));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return output;
    }
    private List<File> getFilesDeeply(File file, List<File> output) throws IOException {
        mPlugin.getLogger().finer(String.format("GetFilesDeep(%s)", file.getAbsolutePath()));
        final File[] listFiles = file.listFiles();
        if (listFiles == null) return output;
        for (File item : listFiles) {
            if (!file.exists()) {
                mPlugin.getLogger().warning(String.format("File(%s) does not exists", file.getAbsolutePath()));
                continue;
            }
            if (isExcluded(item)) {
                mPlugin.getLogger().finer(String.format("Ignore(%s)", item.getAbsolutePath()));
                continue;
            }
            output.add(item);
            if (item.isDirectory()) {
                getFilesDeeply(item, output);
            }
        }
        return output;
    }

    private boolean isExcluded(File file) throws IOException {
        for (File excludedFile : mExcludes) {
            if (file.getCanonicalPath().equals(excludedFile.getCanonicalPath())) {
                return true;
            }
        }
        return false;
    }

}
