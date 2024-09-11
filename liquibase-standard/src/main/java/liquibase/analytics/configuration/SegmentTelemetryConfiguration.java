package liquibase.analytics.configuration;

import liquibase.Scope;
import liquibase.util.Cache;
import lombok.Data;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

@Data
public class SegmentTelemetryConfiguration implements TelemetryConfiguration {
    private final static Cache<RemoteTelemetryConfiguration> remoteTelemetryConfiguration = new Cache<>(() -> {
        String url = TelemetryArgs.CONFIG_ENDPOINT_URL.getCurrentValue();
        AtomicReference<RemoteTelemetryConfiguration> remoteTelemetryConfiguration = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            try {
                InputStream input = new URL(url).openStream();
                Yaml yaml = new Yaml();
                remoteTelemetryConfiguration.set(yaml.loadAs(input, RemoteTelemetryConfiguration.class));
            } catch (Exception e) {
                Scope.getCurrentScope().getLog(SegmentTelemetryConfiguration.class).fine("Failed to load telemetry configuration from " + url, e);
            }
        });
        thread.start();
        thread.join(TelemetryArgs.CONFIG_ENDPOINT_TIMEOUT_MILLIS.getCurrentValue());
        return remoteTelemetryConfiguration.get();
    });

    @Override
    public int getPriority() {
        return 0;
    }

    public int getTimeoutMillis() throws Exception {
        return remoteTelemetryConfiguration.get().getTimeoutMs();
    }

    public String getDestinationUrl() throws Exception {
        return remoteTelemetryConfiguration.get().getEndpointData();
    }

    @Override
    public boolean isOssTelemetryEnabled() throws Exception {
        return remoteTelemetryConfiguration.get().isSendOss();
    }

    @Override
    public boolean isProTelemetryEnabled() throws Exception {
        return remoteTelemetryConfiguration.get().isSendPro();
    }

    public String getWriteKey() throws Exception {
        return remoteTelemetryConfiguration.get().getWriteKey();
    }
}
