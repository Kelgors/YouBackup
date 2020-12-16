package me.kelgors.youbackup.ftp;

import me.kelgors.youbackup.api.configuration.IBackupProfile;
import me.kelgors.youbackup.api.storage.BasicRemoteFile;
import me.kelgors.youbackup.api.storage.IRemoteFile;
import me.kelgors.youbackup.api.storage.Storage;
import org.apache.commons.net.ftp.*;
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
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FtpStorage extends Storage {
    public static final String STORAGE_TYPE = "ftp";
    private static final String ERROR_CONNECT = "Cannot connect to ftp server";

    private String host = null;
    private int port = 21;
    private String username = null;
    private String password = null;
    private String path = ".";

    private FTPClient mClient;

    private final Logger mLogger;

    public FtpStorage(Plugin plugin) {
        super(plugin);
        mLogger = plugin.getLogger();
    }

    @Override
    public String getStorageType() {
        return STORAGE_TYPE;
    }

    @Override
    public void prepare(IBackupProfile config) {
        final ConfigurationSection destination = config.getDestination();
        host = destination.getString("host", null);
        port = destination.getInt("port", 21);
        username = destination.getString("username", null);
        password = destination.getString("password", null);
        path = destination.getString("path", "");
        mClient = new FTPClient();
    }

    @Override
    public CompletableFuture<List<IRemoteFile>> list() {
        final CompletableFuture<List<IRemoteFile>> output = new CompletableFuture<>();
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
                final List<IRemoteFile> files = Arrays.stream(ftpFiles)
                        .map((file) -> new BasicRemoteFile(file.getName(), file.getTimestamp().toInstant().atZone(ZoneId.systemDefault())))
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
    public CompletableFuture<Boolean> delete(IRemoteFile remoteFile) {
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
        mLogger.log(level, YouBackupFtpPlugin.SERVER_TAG + message);
    }
}
