package me.kelgors.ubackup.ftp;

import me.kelgors.ubackup.configuration.BackupConfiguration;
import me.kelgors.ubackup.storage.IStorage;
import me.kelgors.ubackup.storage.RemoteFile;
import org.apache.commons.net.ftp.*;
import org.apache.commons.net.util.TrustManagerUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FtpStorage implements IStorage {
    public static final String STORAGE_TYPE = "ftp";
    private static final String ERROR_CONNECT = "Cannot connect to ftp server";
    private final Plugin mPlugin;

    private String host = null;
    private int port = 21;
    private boolean isSecure = false;
    private String username = null;
    private String password = null;
    private String path = ".";

    private FTPClient mClient;

    public FtpStorage(Plugin plugin) {
        mPlugin = plugin;
    }

    @Override
    public String getStorageType() {
        return STORAGE_TYPE;
    }

    @Override
    public void prepare(BackupConfiguration config) {
        final ConfigurationSection destination = config.getDestination();
        if (destination.contains("secure")) {
            isSecure = config.getDestination().getBoolean("secure", false);
        }
        if (destination.contains("host")) {
            host = destination.getString("host", null);
        }
        if (destination.contains("port")) {
            port = destination.getInt("port", 21);
        }
        if (destination.contains("username")) {
            username = destination.getString("username", null);
        }
        if (destination.contains("password")) {
            password = destination.getString("password", null);
        }
        if (destination.contains("path")) {
            path = destination.getString("path", "");
        }
        if (isSecure) {
            FTPSClient sftp = new FTPSClient();
            sftp.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
            mClient = sftp;
        } else {
            mClient = new FTPClient();
        }
    }

    @Override
    public CompletableFuture<List<RemoteFile>> list() {
        final CompletableFuture<List<RemoteFile>> output = new CompletableFuture<>();
        runTaskAsync(() -> {
            try {
                if (!mClient.isConnected() && !connect()) {
                    output.completeExceptionally(new Exception(ERROR_CONNECT));
                    return;
                }
                // ensure we are in correct directory
                mClient.changeWorkingDirectory(path);
                // list files on ftp server in given path
                final FTPFile[] ftpFiles = mClient.listFiles();
                // transform FTPFile to RemoteFile
                final List<RemoteFile> files = Arrays.stream(ftpFiles)
                        .map((file) -> new RemoteFile(file.getName(), file.getTimestamp().toInstant().atZone(ZoneId.systemDefault())))
                        .collect(Collectors.toList());
                output.complete(files);
            } catch (IOException e) {
                e.printStackTrace();
                output.completeExceptionally(e);
            }
        });
        return output;
    }

    @Override
    public CompletableFuture<Boolean> create(File file) {
        final CompletableFuture<Boolean> output = new CompletableFuture<>();
        runTaskAsync(() -> {
            try (final FileInputStream inputStream = new FileInputStream(file)) {
                if (!mClient.isConnected() && !connect()) {
                    output.completeExceptionally(new Exception(ERROR_CONNECT));
                    return;
                }
                // ensure directory exists
                final int replyMkd = ensureDirectoryPresence(path);
                if (replyMkd == FTPReply.PATHNAME_CREATED) {
                    log(Level.INFO, "Uploading file " + file.getName());
                    log(Level.FINER, "STOR " + file.getName());
                    // upload file
                    if (!mClient.storeFile(file.getName(), inputStream)) {
                        log(Level.WARNING, "Unable to upload file to " + path);
                        output.complete(false);
                    } else {
                        output.complete(true);
                    }
                } else {
                    log(Level.SEVERE, "Cannot create folder " + path);
                    log(Level.SEVERE, "FTPError#" + mClient.getReplyString());
                    output.complete(false);
                }
                mClient.noop();
            }
            catch (final FTPConnectionClosedException ex)
            {
                log(Level.SEVERE, "Server closed connection.");
                ex.printStackTrace();
                output.completeExceptionally(ex);
            }
            catch (final IOException ex) {
                ex.printStackTrace();
                output.completeExceptionally(ex);
            }
        });
        return output;
    }

    @Override
    public CompletableFuture<Boolean> delete(RemoteFile remoteFile) {
        final CompletableFuture<Boolean> output = new CompletableFuture<>();
        runTaskAsync(() -> {
            try {
                if (!mClient.isConnected() && !connect()) {
                    output.completeExceptionally(new Exception(ERROR_CONNECT));
                    return;
                }
                // ensure directory exists
                mClient.changeWorkingDirectory(path);
                // delete file
                output.complete(mClient.deleteFile(remoteFile.getName()));
                mClient.noop();
            } catch (IOException e) {
                e.printStackTrace();
                output.completeExceptionally(e);
            }
        });
        return output;
    }

    @Override
    public CompletableFuture<Boolean> close() {
        final CompletableFuture<Boolean> output = new CompletableFuture<>();
        runTaskAsync(() -> {
            try {
                mClient.logout();
                output.complete(true);
            } catch (IOException e) {
                e.printStackTrace();
                output.completeExceptionally(e);
            } finally {
                try {
                    mClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return output;
    }

    private boolean connect() throws IOException {
        log(Level.INFO, String.format("Connecting to %s:%d...", host, port));
        mClient.connect(host, port);
        log(Level.INFO, "Logging in...");
        if (!mClient.login(username, password)) {
            log(Level.SEVERE, "Cannot login to ftp server!");
            return false;
        }
        mClient.enterLocalPassiveMode();
        mClient.setUseEPSVwithIPv4(true);
        mClient.setFileStructure(FTP.BINARY_FILE_TYPE);
        return true;
    }

    private int ensureDirectoryPresence(String path) throws IOException {
        final FTPClient client = mClient;
        final String[] paths = path.split(Pattern.quote(File.separator));
        for (String folder : paths) {
            if (".".equals(folder)) continue;
            if (!"..".equals(folder)) {
                log(Level.FINER, "MKD " + folder);
                int mkdReply = client.mkd(folder);
                String message = client.getReplyString();
                log(Level.FINER, String.format("%d %s", mkdReply, message));
                if (!message.contains("File exists") && mkdReply != FTPReply.PATHNAME_CREATED) {
                    return mkdReply;
                }
            }
            log(Level.FINER, "CWD " + folder);
            int cwdReply = client.cwd(folder);
            log(Level.FINER, String.format("%s %s", cwdReply, client.getReplyString()));
        }
        return FTPReply.PATHNAME_CREATED;
    }

    private void log(Level level, String message) {
        mPlugin.getLogger().log(level, uBackupFtpPlugin.SERVER_TAG + message);
    }

    private void runTaskAsync(final Runnable runnable) {
        mPlugin.getServer().getScheduler().runTaskAsynchronously(mPlugin, runnable);
    }
}
