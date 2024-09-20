package liquibase.analytics.configuration;

import liquibase.Scope;
import liquibase.logging.Logger;
import liquibase.util.Cache;
import lombok.Data;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@Data
public class SegmentAnalyticsConfiguration implements AnalyticsConfiguration {
    private final static Cache<RemoteAnalyticsConfiguration> remoteAnalyticsConfiguration = new Cache<>(() -> {
        /**
         * It is important to obtain the URL here outside of the newly created thread. {@link Scope} stores its stuff
         * in a ThreadLocal, so if you tried to get the value inside the thread, the value could be different.
         */
        Logger log = Scope.getCurrentScope().getLog(SegmentAnalyticsConfiguration.class);
        Level logLevel = AnalyticsArgs.LOG_LEVEL.getCurrentValue();
        String url = AnalyticsArgs.CONFIG_ENDPOINT_URL.getCurrentValue();
        AtomicReference<RemoteAnalyticsConfiguration> remoteAnalyticsConfiguration = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            try {
                InputStream input = new URL(url).openStream();
                Yaml yaml = new Yaml();
                remoteAnalyticsConfiguration.set(yaml.loadAs(input, RemoteAnalyticsConfiguration.class));
            } catch (Exception e) {
                log.log(logLevel, "Failed to load analytics configuration from " + url, e);
            }
        });
        thread.start();
        thread.join(AnalyticsArgs.CONFIG_ENDPOINT_TIMEOUT_MILLIS.getCurrentValue());
        return remoteAnalyticsConfiguration.get();
    });

    @Override
    public int getPriority() {
        return 0;
    }

    public int getTimeoutMillis() throws Exception {
        return remoteAnalyticsConfiguration.get().getTimeoutMs();
    }

    public String getDestinationUrl() throws Exception {
        return remoteAnalyticsConfiguration.get().getEndpointData();
    }

    @Override
    public boolean isOssAnalyticsEnabled() throws Exception {
        return remoteAnalyticsConfiguration.get().isSendOss();
    }

    @Override
    public boolean isProAnalyticsEnabled() throws Exception {
        return remoteAnalyticsConfiguration.get().isSendPro();
    }

    public String getWriteKey() throws Exception {
        return remoteAnalyticsConfiguration.get().getWriteKey();
    }

    public List<RemoteAnalyticsConfiguration.ExtensionName> getExtensionNames() throws Exception {
        return remoteAnalyticsConfiguration.get().getExtensions();
    }
}
