package me.kelgors.youbackup.s3;

import me.kelgors.youbackup.api.configuration.IBackupProfile;
import me.kelgors.youbackup.api.storage.BasicRemoteFile;
import me.kelgors.youbackup.api.storage.IRemoteFile;
import me.kelgors.youbackup.api.storage.Storage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.File;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class S3Storage extends Storage {

    public static final String STORAGE_TYPE = "s3";
    private final Logger mLogger;
    String mClientId;
    String mClientSecret;
    String mBucket;
    String mPath = "/";
    String mRegion = "eu-west-3";
    String mProfileName;

    S3AsyncClient mClient;

    public S3Storage(Plugin plugin) {
        super(plugin);
        mLogger = plugin.getLogger();
    }

    @Override
    public String getStorageType() {
        return STORAGE_TYPE;
    }

    @Override
    public void prepare(IBackupProfile config) {
        // get config from yaml
        final ConfigurationSection destination = config.getDestination();
        mClientId = destination.getString("client_id", null);
        mClientSecret = destination.getString("client_secret", null);
        mBucket = destination.getString("bucket", null);
        mPath = destination.getString("path", null);
        mRegion = destination.getString("region", "global");
        mProfileName = destination.getString("profile", "");
        // test connection if relevant
        AwsCredentialsProvider credentials;
        if (mProfileName != null && mProfileName.length() > 0) {
            mLogger.finer("Prepare AWS with profile mode");
            credentials = ProfileCredentialsProvider.create(mProfileName);
        } else {
            mLogger.finer("Prepare AWS with credentials mode");
            credentials = StaticCredentialsProvider.create(AwsBasicCredentials.create(mClientId, mClientSecret));
        }
        // create credentials
        mClient = S3AsyncClient.builder()
                .region(Region.of(mRegion))
                .credentialsProvider(credentials)
                .build();
    }

    @Override
    public CompletableFuture<List<IRemoteFile>> list() {
        final ListObjectsRequest request = ListObjectsRequest.builder()
                .bucket(mBucket)
                .prefix(mPath)
                .build();
        return mClient.listObjects(request)
                .thenCompose((res) -> {
                    return CompletableFuture.completedFuture(
                        res.contents().stream()
                            .map((object) -> new BasicRemoteFile(object.key(), object.lastModified().atZone(ZoneId.systemDefault())))
                            .collect(Collectors.toList())
                    );
                });
    }

    @Override
    public CompletableFuture<Boolean> create(File file) {
        final String filename = file.getName();
        // prepare request
        final PutObjectRequest request = PutObjectRequest.builder()
                .key(Paths.get(mPath, filename).toString())
                .bucket(mBucket)
                .build();
        // prepare request-body
        final AsyncRequestBody body = AsyncRequestBody.fromFile(file);
        // upload object
        mLogger.info(String.format("Uploading %s to s3:%s/%s", file.getName(), mBucket, mPath));
        final CompletableFuture<PutObjectResponse> future = mClient.putObject(request, body);
        future.whenComplete((r, t) -> {
            if (t == null && r != null) {
                mLogger.info("Upload success");
            } else if (t != null) {
                mLogger.severe("Upload failure");
                t.printStackTrace();
            }
            if (file.delete()) {
                mLogger.info("Temporary backup file successfully deleted");
            } else {
                mLogger.warning("Unable to delete temporary backup file");
            }
        });
        return future.thenCompose(this::onS3UploadComplete);
    }

    @Override
    public CompletableFuture<Boolean> delete(IRemoteFile remoteFile) {
        final DeleteObjectRequest request = DeleteObjectRequest.builder()
                .key(remoteFile.getName())
                .bucket(mBucket)
                .build();
        mLogger.info(String.format("Deleting %s to s3:%s/%s", remoteFile.getName(), mBucket, mPath));
        return mClient.deleteObject(request)
                .thenCompose((res) -> CompletableFuture.completedFuture(res.deleteMarker()));
    }

    @Override
    public CompletableFuture<Boolean> close() {
        mClient.close();
        return CompletableFuture.completedFuture(true);
    }

    private CompletableFuture<Boolean> onS3UploadComplete(PutObjectResponse putObjectResponse) {
        final CompletableFuture<Boolean> output = new CompletableFuture<>();
        if (putObjectResponse != null) {
            mLogger.info("Object uploaded. Details: " + putObjectResponse);
            output.complete(true);
        } else {
            mLogger.info("No response from AWS:S3");
            output.complete(false);
        }
        return output;
    }

}
