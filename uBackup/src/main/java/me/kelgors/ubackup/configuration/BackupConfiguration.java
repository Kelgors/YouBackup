package me.kelgors.ubackup.configuration;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.bukkit.configuration.ConfigurationSection;

import java.time.ZonedDateTime;
import java.util.Optional;

public class BackupConfiguration {

    private final String filename;
    private final String name;
    private final int rotation;
    private boolean enabled = false;
    private final ConfigurationSection compression;
    private final ConfigurationSection destination;
    private Cron cron;
    private ZonedDateTime nextExecutionTime;

    public static BackupConfiguration parse(String name, ConfigurationSection section) {
        // section.isString("destination")
        return new BackupConfiguration(name, section.getString("filename", "{uuid}.zip"),
                section.getBoolean("enabled", false),
                section.getInt("rotation", 0),
                section.getString("cron", null),
                section.getConfigurationSection("compression"),
                section.getConfigurationSection("destination")
        );
    }

    public BackupConfiguration(String name, String filename, boolean enabled, int rotation, String cronExpression, ConfigurationSection compression, ConfigurationSection destination) {
        this.name = name;
        this.filename = filename;
        this.enabled = enabled;
        this.rotation = rotation;
        this.compression = compression;
        this.destination = destination;
        parseCron(cronExpression);
    }

    void parseCron(String expression) {
        final CronDefinition definition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        final CronParser parser = new CronParser(definition);
        cron = parser.parse(expression);
        cron.validate();
        resetNextExecutionTime();
    }

    public ZonedDateTime getNextExecutionTime() {
        return nextExecutionTime;
    }

    public ZonedDateTime calculateNextExecutionTime() {
        final ExecutionTime time = ExecutionTime.forCron(cron);
        final Optional<ZonedDateTime> datetime = time.nextExecution(ZonedDateTime.now());
        return (nextExecutionTime = datetime.orElse(null));
    }

    /**
     * Get the next execution time as server ticks for this backup configuration
     * This value is cached until the {resetNextExecutionTime} is called
     * @return
     */
    public Long getNextExecutionRemainingTicks() {
        final ZonedDateTime nextExecutionTime = getNextExecutionTime();
        if (nextExecutionTime == null) return null;
        return (nextExecutionTime.toInstant().toEpochMilli() - ZonedDateTime.now().toInstant().toEpochMilli()) / 1000 * 20;
    }

    /**
     *
     */
    public void resetNextExecutionTime() {
        nextExecutionTime = calculateNextExecutionTime();
    }

    public String getFilename() {
        return filename;
    }

    public String getName() {
        return name;
    }

    public int getRotation() {
        return rotation;
    }

    public ConfigurationSection getCompression() {
        return compression;
    }

    public ConfigurationSection getDestination() {
        return destination;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
