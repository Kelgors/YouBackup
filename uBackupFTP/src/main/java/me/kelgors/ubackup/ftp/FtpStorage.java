package me.kelgors.ubackup.ftp;

import me.kelgors.ubackup.configuration.BackupConfiguration;
import me.kelgors.ubackup.storage.IStorage;
import org.apache.commons.net.ftp.*;
import org.apache.commons.net.util.TrustManagerUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class FtpStorage implements IStorage {
    public static final String STORAGE_TYPE = "ftp";
    private final Plugin mPlugin;

    private String host = null;
    private int port = 21;
    private boolean isSecure = false;
    private String username = null;
    private String password = null;
    private String path = ".";

    public FtpStorage(Plugin plugin) {
        mPlugin = plugin;
    }

    @Override
    public String getStorageType() {
        return STORAGE_TYPE;
    }

    @Override
    public void prepare(BackupConfiguration config) {
        final ConfigurationSection destination = config.destination;
        if (destination.contains("secure")) {
            isSecure = config.destination.getBoolean("secure", false);
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
    }

    @Override
    public CompletableFuture<Boolean> backup(File file) {
        final CompletableFuture<Boolean> output = new CompletableFuture<>();
        mPlugin.getServer().getScheduler().runTaskAsynchronously(mPlugin, () -> {
            final FTPClient client;
            if (isSecure) {
                FTPSClient sftp = new FTPSClient();
                sftp.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
                client = sftp;
            } else {
                client = new FTPClient();
            }
            try (final FileInputStream inputStream = new FileInputStream(file)) {
                log(Level.INFO, String.format("Connecting to %s:%d...", host, port));
                client.connect(host, port);
                log(Level.INFO, "Logging in...");
                if (!client.login(username, password)) {
                    log(Level.SEVERE, "Cannot login to ftp server!");
                    output.complete(false);
                    return;
                }
                client.enterLocalPassiveMode();
                client.setUseEPSVwithIPv4(true);
                client.setFileStructure(FTP.BINARY_FILE_TYPE);
                final int replyMkd = ensureDirectoryPresence(client, path);
                if (replyMkd == FTPReply.PATHNAME_CREATED) {
                    log(Level.INFO, "Uploading file " + file.getName());
                    log(Level.FINER, "STOR " + file.getName());
                    if (!client.storeFile(file.getName(), inputStream)) {
                        log(Level.WARNING, "Unable to upload file to " + path);
                        output.complete(false);
                    } else {
                        output.complete(true);
                    }
                } else {

                    log(Level.SEVERE, "Cannot create folder " + path);
                    log(Level.SEVERE, "FTPError#"+client.getReplyString());
                    output.complete(false);
                }
                client.noop();
                client.logout();
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
            finally {
                if (client.isConnected()) {
                    try {
                        log(Level.INFO, "Disconnecting...");
                        client.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return output;
    }

    private int ensureDirectoryPresence(FTPClient client, String path) throws IOException {
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
}
