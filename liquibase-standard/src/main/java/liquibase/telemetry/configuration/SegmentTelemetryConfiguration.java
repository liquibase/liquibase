package liquibase.telemetry.configuration;

import liquibase.util.Cache;
import lombok.Data;

@Data
public class SegmentTelemetryConfiguration implements TelemetryConfiguration {
    private final static Cache<RemoteTelemetryConfiguration> remoteTelemetryConfiguration = new Cache<>(() -> {
        // todo this needs to be fetched from the config endpoint
        // todo this should only wait a certain amount of time before it too gives up
        return new RemoteTelemetryConfiguration(
                1500000000,
                "https://api.segment.io/v1/batch",
                true,
                false,
                TelemetryArgs.WRITE_KEY.getCurrentValue()
        );
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

    public boolean isOssTelemetryEnabled() throws Exception {
        return remoteTelemetryConfiguration.get().isEnabledOss();
    }

    public boolean isProTelemetryEnabled() throws Exception {
        return remoteTelemetryConfiguration.get().isEnabledPro();
    }

    public String getWriteKey() throws Exception {
        return remoteTelemetryConfiguration.get().getWriteKey();
    }
}
