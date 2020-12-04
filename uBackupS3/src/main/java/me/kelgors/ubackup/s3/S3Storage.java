package me.kelgors.ubackup.s3;

import me.kelgors.ubackup.configuration.BackupConfiguration;
import me.kelgors.ubackup.storage.IStorage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class S3Storage implements IStorage {

    public static final String STORAGE_TYPE = "s3";
    String mClientId;
    String mClientSecret;
    String mBucket;
    String mPath = "/";
    String mRegion = "eu-west-3";
    String mProfileName;

    Plugin mPlugin;
    S3AsyncClient mClient;

    public S3Storage(Plugin plugin) {
        mPlugin = plugin;
    }

    @Override
    public String getStorageType() {
        return STORAGE_TYPE;
    }

    @Override
    public void prepare(BackupConfiguration config) {
        // get config from yaml
        final ConfigurationSection destination = config.destination;
        mClientId = destination.getString("client_id", null);
        mClientSecret = destination.getString("client_secret", null);
        mBucket = destination.getString("bucket", null);
        mPath = destination.getString("path", null);
        mRegion = destination.getString("region", "global");
        mProfileName = destination.getString("profile", "");
        // test connection if relevant
        AwsCredentialsProvider credentials;
        if (mProfileName != null && mProfileName.length() > 0) {
            mPlugin.getLogger().finer("Prepare AWS with profile mode");
            credentials = ProfileCredentialsProvider.create(mProfileName);
        } else {
            mPlugin.getLogger().finer("Prepare AWS with credentials mode");
            credentials = StaticCredentialsProvider.create(AwsBasicCredentials.create(mClientId, mClientSecret));
        }
        // create credentials
        mClient = S3AsyncClient.builder()
                .region(Region.of(mRegion))
                .credentialsProvider(credentials)
                .build();
    }

    @Override
    public CompletableFuture<Boolean> backup(File file) {
        final Logger logger = mPlugin.getLogger();
        final String filename = file.getName();
        // prepare request
        final PutObjectRequest request = PutObjectRequest.builder()
                .key(Paths.get(mPath, filename).toString())
                .bucket(mBucket)
                .build();
        // prepare request-body
        final AsyncRequestBody body = AsyncRequestBody.fromFile(file);
        // upload object
        logger.info(String.format("[S3Storage] Uploading %s to s3:%s/%s", file.getName(), mBucket, mPath));
        final CompletableFuture<PutObjectResponse> future = mClient.putObject(request, body);
        future.whenComplete((r, t) -> {
            if (t == null && r != null) {
                logger.info("[S3Storage] Upload success");
            } else if (t != null) {
                logger.severe("[S3Storage] Upload failure");
                t.printStackTrace();
            }
            if (file.delete()) {
                logger.info("[S3Storage] Temporary backup file successfully deleted");
            } else {
                logger.warning("[S3Storage] Unable to delete temporary backup file");
            }
        });
        return future.thenCompose(this::onS3UploadComplete);
    }

    private CompletableFuture<Boolean> onS3UploadComplete(PutObjectResponse putObjectResponse) {
        final CompletableFuture<Boolean> output = new CompletableFuture<>();
        mClient.close();
        if (putObjectResponse != null) {
            mPlugin.getLogger().info("[S3Storage] Object uploaded. Details: " + putObjectResponse);
            output.complete(true);
        } else {
            mPlugin.getLogger().info("No response from AWS:S3");
            output.complete(false);
        }

        return output;
    }

}
