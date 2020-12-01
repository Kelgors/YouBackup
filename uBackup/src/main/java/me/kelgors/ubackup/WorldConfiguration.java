package me.kelgors.ubackup;

import java.util.Map;

public class WorldConfiguration {
    public String name;
    public String filename;
    public boolean enabled;
    public String compression;
    public Map<String, Object> destination;

    public String toString() {
        String type = "null";
        if (destination != null && destination.containsKey("type")) {
            type = (String) destination.get("type");
        }
        return String.format("WorldConfiguration<%s>(compression: %s, type: %s, enabled: %b)", name, compression, type, enabled);
    }
}
