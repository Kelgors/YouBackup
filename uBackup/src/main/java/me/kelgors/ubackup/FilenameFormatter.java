package me.kelgors.ubackup;

import org.bukkit.World;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class FilenameFormatter {

    public static String format(String pattern, LocalDateTime datetime, World world) {
        final String uuid = UUID.randomUUID().toString();
        return pattern
                .replaceAll("\\{timestamp\\}", String.valueOf(datetime.toInstant(ZoneOffset.UTC).getEpochSecond()))
                .replaceAll("\\{date\\}", datetime.format(DateTimeFormatter.BASIC_ISO_DATE))
                .replaceAll("\\{time\\}", datetime.format(DateTimeFormatter.ofPattern("HHmmss")))
                .replaceAll("\\{datetime\\}", datetime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
                .replaceAll("\\{world\\}", world.getName())
                .replaceAll("\\{uuid\\}", uuid);

    }
}
