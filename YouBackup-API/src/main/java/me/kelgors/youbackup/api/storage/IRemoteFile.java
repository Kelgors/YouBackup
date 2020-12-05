package me.kelgors.youbackup.api.storage;

import java.time.ZonedDateTime;

public interface IRemoteFile {
    String getName();
    ZonedDateTime getCreatedAt();
}
