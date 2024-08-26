package liquibase.telemetry.configuration;

import lombok.Data;

@Data
public class RemoteTelemetryConfiguration {
    private final int timeoutMs;
    private final String endpointData;
    private final boolean enabledOss;
    private final boolean enabledPro;
    private final String writeKey;
}
