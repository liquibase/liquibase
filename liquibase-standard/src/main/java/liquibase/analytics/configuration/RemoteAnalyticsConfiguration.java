package liquibase.analytics.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class RemoteAnalyticsConfiguration {
    private int timeoutMs;
    private String endpointData;
    private boolean sendOss;
    private boolean sendPro;
    private String writeKey;
    private List<ExtensionName> extensions;

    @Data
    public static class ExtensionName {
        /**
         * The name of the extension in the manifest file.
         */
        private String manifestName;
        /**
         * The name that should be used when transmitting the analytics event to the destination.
         */
        private String displayName;
    }
}
