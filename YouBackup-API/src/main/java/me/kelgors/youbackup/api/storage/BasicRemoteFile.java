package me.kelgors.youbackup.api.storage;

import java.time.ZonedDateTime;

public class BasicRemoteFile implements IRemoteFile {
    private final String mFilename;
    private final ZonedDateTime mCreatedAt;

    public BasicRemoteFile(String filename, ZonedDateTime createdAt) {
        mFilename = filename;
        mCreatedAt = createdAt;
    }

    public String getName() {
        return mFilename;
    }

    public ZonedDateTime getCreatedAt() {
        return mCreatedAt;
    }
}
