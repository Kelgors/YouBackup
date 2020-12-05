package me.kelgors.ubackup.configuration;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import me.kelgors.ubackup.api.configuration.IBackupConfiguration;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Proxy;
import java.time.ZonedDateTime;
import java.util.Optional;

public class BackupConfiguration implements IBackupConfiguration {

    private final String filename;
    private final String name;
    private final int rotation;
    private boolean enabled = false;
    private final ConfigurationSection compression;
    private final ConfigurationSection destination;
    private Cron cron;
    private ZonedDateTime nextExecutionTime;

    public BackupConfiguration(String name, ConfigurationSection profile) {
        this.name = name;
        this.filename = profile.getString("filename", "{uuid}.zip");
        this.enabled = profile.getBoolean("enabled", false);
        this.rotation = profile.getInt("rotation", 0);
        final ConfigurationSection compression = profile.getConfigurationSection("compression");
        if (compression != null) {
            this.compression = (ConfigurationSection) Proxy.newProxyInstance(
                ConfigurationSection.class.getClassLoader(),
                new Class[]{ConfigurationSection.class},
                new ConfigurationSectionHandler(compression)
            );
        } else {
            this.compression = null;
        }
        final ConfigurationSection destination = profile.getConfigurationSection("destination");
        if (destination != null) {
            this.destination = (ConfigurationSection) Proxy.newProxyInstance(
                ConfigurationSection.class.getClassLoader(),
                new Class[]{ConfigurationSection.class},
                new ConfigurationSectionHandler(destination)
            );
        } else {
            this.destination = null;
        }
        parseCron(profile.getString("cron"));
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
