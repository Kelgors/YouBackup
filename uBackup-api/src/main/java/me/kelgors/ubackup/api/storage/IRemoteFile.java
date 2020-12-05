package me.kelgors.ubackup.api.storage;

import java.time.ZonedDateTime;

public interface IRemoteFile {
    String getName();
    ZonedDateTime getCreatedAt();
}
