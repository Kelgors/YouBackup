package me.kelgors.ubackup.s3;

import com.google.common.io.Files;
import me.kelgors.ubackup.WorldConfiguration;
import me.kelgors.ubackup.storage.IStorage;
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
import java.time.LocalDateTime;
import java.util.Map;
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
    public void prepare(WorldConfiguration config) {
        // get config from yaml
        Map<String, Object> destination = config.destination;
        mClientId = (String) destination.get("client_id");
        mClientSecret = (String) destination.get("client_secret");
        mBucket = (String) destination.get("bucket");
        mPath = (String) destination.get("path");
        mRegion = (String) destination.get("region");
        mProfileName = (String) destination.get("profile");
        // test connection if relevant
        AwsCredentialsProvider credentials;
        if (mProfileName != null && mProfileName.length() > 0) {
            credentials = ProfileCredentialsProvider.create(mProfileName);
        } else {
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
        final LocalDateTime today = LocalDateTime.now();
        // prepare a filename base on (year)(month)(day)T(hour)(minute)(second)_(world_name).(ext)
        final String filename = String.format("%d%02d%02dT%02d%02d%02d_%s", today.getYear(), today.getMonth().getValue(), today.getDayOfMonth(), today.getHour(), today.getMinute(), today.getSecond(), file.getName());
        // prepare request
        final PutObjectRequest request = PutObjectRequest.builder()
                .key(Paths.get(mPath, filename).toString())
                .bucket(mBucket)
                .build();
        // prepare request-body
        final AsyncRequestBody body = AsyncRequestBody.fromFile(file);
        // upload object
        logger.info(String.format("[S3Storage] Sending %s to s3:%s/%s", file.getName(), mBucket, mPath));
        final CompletableFuture<PutObjectResponse> future = mClient.putObject(request, body);
        future.whenComplete((r, t) -> {
            if (file.delete()) {
                logger.info("[S3Storage] Temporary backup file successfully deleted on server");
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
