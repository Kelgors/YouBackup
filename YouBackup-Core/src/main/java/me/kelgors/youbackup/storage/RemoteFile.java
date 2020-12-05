package me.kelgors.youbackup.storage;

import java.time.ZonedDateTime;

public class RemoteFile {

    private final String mFilename;
    private final ZonedDateTime mCreatedAt;

    public RemoteFile(String filename, ZonedDateTime createdAt) {
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
