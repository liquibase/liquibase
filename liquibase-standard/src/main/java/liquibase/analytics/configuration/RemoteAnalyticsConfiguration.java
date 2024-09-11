package liquibase.analytics.configuration;

import lombok.Data;

@Data
public class RemoteAnalyticsConfiguration {
    private int timeoutMs;
    private String endpointData;
    private boolean sendOss;
    private boolean sendPro;
    private String writeKey;
}
