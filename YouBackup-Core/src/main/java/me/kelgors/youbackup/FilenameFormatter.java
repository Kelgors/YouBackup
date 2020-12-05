package me.kelgors.youbackup;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class FilenameFormatter {

    public static String format(String pattern, LocalDateTime datetime, String backupName) {
        final String uuid = UUID.randomUUID().toString();
        return pattern
                .replaceAll("\\{timestamp\\}", String.valueOf(datetime.toInstant(ZoneOffset.UTC).getEpochSecond()))
                .replaceAll("\\{date\\}", datetime.format(DateTimeFormatter.BASIC_ISO_DATE))
                .replaceAll("\\{time\\}", datetime.format(DateTimeFormatter.ofPattern("HHmmss")))
                .replaceAll("\\{datetime\\}", datetime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
                .replaceAll("\\{world\\}", backupName)
                .replaceAll("\\{uuid\\}", uuid);

    }
}
