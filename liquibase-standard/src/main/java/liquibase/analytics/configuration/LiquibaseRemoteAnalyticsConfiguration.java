package liquibase.analytics.configuration;

import liquibase.Scope;
import liquibase.configuration.ConfiguredValue;
import liquibase.logging.Logger;
import liquibase.util.Cache;
import lombok.Data;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

/**
 * LiquibaseRemoteAnalyticsConfiguration is responsible for fetching and providing
 * remote analytics configurations used by Liquibase.
 *
 * This class uses a {@link Cache} to store the remote analytics configuration, which is
 * loaded from a specified URL endpoint.
 */
@Data
public class LiquibaseRemoteAnalyticsConfiguration implements AnalyticsConfiguration {

    /**
     * Cache holding the {@link RemoteAnalyticsConfiguration} object. It ensures the
     * configuration is fetched only once and reused across the application.
     *
     * The configuration is loaded from the URL specified by {@link AnalyticsArgs#CONFIG_ENDPOINT_URL}.
     */
    private final static Cache<RemoteAnalyticsConfiguration> remoteAnalyticsConfiguration = new Cache<>(() -> {
        /**
         * It is important to obtain the URL here outside of the newly created thread. {@link Scope} stores its stuff
         * in a ThreadLocal, so if you tried to get the value inside the thread, the value could be different.
         */
        Logger log = Scope.getCurrentScope().getLog(AnalyticsConfiguration.class);
        Level logLevel = AnalyticsArgs.LOG_LEVEL.getCurrentValue();
        String url = AnalyticsArgs.CONFIG_ENDPOINT_URL.getCurrentValue();
        AtomicReference<RemoteAnalyticsConfiguration> remoteAnalyticsConfiguration = new AtomicReference<>();
        AtomicBoolean timedOut = new AtomicBoolean(true);
        Thread thread = new Thread(() -> {
            try {
                InputStream input = new URL(url).openStream();
                Yaml yaml = new Yaml();
                Map<String, Object> loaded = yaml.loadAs(input, Map.class);
                remoteAnalyticsConfiguration.set(RemoteAnalyticsConfiguration.fromYaml(loaded));
            } catch (Exception e) {
                log.log(logLevel, "Failed to load analytics configuration from " + url, e);
            }
            timedOut.set(false);
        });
        thread.start();
        thread.join(AnalyticsArgs.CONFIG_ENDPOINT_TIMEOUT_MILLIS.getCurrentValue());
        if (timedOut.get()) {
            log.log(logLevel, "Timed out while attempting to load analytics configuration from " + url, null);
        }
        return remoteAnalyticsConfiguration.get();
    }, false, AnalyticsArgs.CONFIG_CACHE_TIMEOUT_MILLIS.getCurrentValue());

    @Override
    public int getPriority() {
        return 0;
    }

    /**
     * Retrieves the timeout value in milliseconds from the remote configuration.
     *
     * @return the timeout in milliseconds
     * @throws Exception if there is an issue fetching the configuration
     */
    public int getTimeoutMillis() throws Exception {
        ConfiguredValue<Integer> userTimeoutMillis = AnalyticsArgs.TIMEOUT_MILLIS.getCurrentConfiguredValue();
        if (userTimeoutMillis.found()) {
            return userTimeoutMillis.getValue();
        } else {
            return remoteAnalyticsConfiguration.get().getTimeoutMs();
        }
    }

    /**
     * Retrieves the destination URL for analytics from the remote configuration.
     *
     * @return the destination URL as a String
     * @throws Exception if there is an issue fetching the configuration
     */
    public String getDestinationUrl() throws Exception {
        return remoteAnalyticsConfiguration.get().getEndpointData();
    }

    /**
     * Determines if OSS analytics are enabled by reading the remote configuration.
     *
     * @return true if OSS analytics are enabled, false otherwise
     * @throws Exception if there is an issue fetching the configuration
     */
    @Override
    public boolean isOssAnalyticsEnabled() throws Exception {
        return Optional.ofNullable(remoteAnalyticsConfiguration.get())
                .map(RemoteAnalyticsConfiguration::isSendOss)
                .orElse(false);
    }

    /**
     * Determines if Pro analytics are enabled by reading the remote configuration.
     *
     * @return true if Pro analytics are enabled, false otherwise
     * @throws Exception if there is an issue fetching the configuration
     */
    @Override
    public boolean isProAnalyticsEnabled() throws Exception {
        return Optional.ofNullable(remoteAnalyticsConfiguration.get())
                .map(RemoteAnalyticsConfiguration::isSendPro)
                .orElse(false);
    }

    /**
     * Retrieves the write key used for analytics reporting from the remote configuration.
     *
     * @return the write key as a String
     * @throws Exception if there is an issue fetching the configuration
     */
    public String getWriteKey() throws Exception {
        return remoteAnalyticsConfiguration.get().getWriteKey();
    }

    /**
     * Retrieves the list of extension names included in the remote configuration.
     *
     * @return a list of extension names
     * @throws Exception if there is an issue fetching the configuration
     */
    public List<RemoteAnalyticsConfiguration.ExtensionName> getExtensionNames() throws Exception {
        return remoteAnalyticsConfiguration.get().getExtensions();
    }
}
