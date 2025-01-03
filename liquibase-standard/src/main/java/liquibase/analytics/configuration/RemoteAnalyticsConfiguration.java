package liquibase.analytics.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@FieldNameConstants
public class RemoteAnalyticsConfiguration {
    private int timeoutMs;
    private String endpointData;
    private boolean sendOss;
    private boolean sendPro;
    private String writeKey;
    private List<ExtensionName> extensions;

    @Data
    @FieldNameConstants
    @AllArgsConstructor
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

    public static RemoteAnalyticsConfiguration fromYaml(Map<String, Object> loaded) {
        RemoteAnalyticsConfiguration remoteAnalyticsConfiguration = new RemoteAnalyticsConfiguration();
        remoteAnalyticsConfiguration.setTimeoutMs(NumberUtils.toInt(String.valueOf(loaded.get(RemoteAnalyticsConfiguration.Fields.timeoutMs))));
        remoteAnalyticsConfiguration.setEndpointData(String.valueOf(loaded.get(RemoteAnalyticsConfiguration.Fields.endpointData)));
        remoteAnalyticsConfiguration.setSendOss(BooleanUtils.toBoolean(String.valueOf(loaded.get(RemoteAnalyticsConfiguration.Fields.sendOss))));
        remoteAnalyticsConfiguration.setSendPro(BooleanUtils.toBoolean(String.valueOf(loaded.get(RemoteAnalyticsConfiguration.Fields.sendPro))));
        remoteAnalyticsConfiguration.setWriteKey(String.valueOf(loaded.get(RemoteAnalyticsConfiguration.Fields.writeKey)));

        remoteAnalyticsConfiguration.setExtensions(new ArrayList<>());
        Object extensions = loaded.get(RemoteAnalyticsConfiguration.Fields.extensions);
        if (extensions instanceof List) {
            for (Object extension : ((List<?>) extensions)) {
                if (extension instanceof Map) {
                    Object displayName = ((Map<?, ?>) extension).get(RemoteAnalyticsConfiguration.ExtensionName.Fields.displayName);
                    Object manifestName = ((Map<?, ?>) extension).get(RemoteAnalyticsConfiguration.ExtensionName.Fields.manifestName);

                    remoteAnalyticsConfiguration.getExtensions().add(new RemoteAnalyticsConfiguration.ExtensionName(String.valueOf(manifestName), String.valueOf(displayName)));
                }
            }
        }

        return remoteAnalyticsConfiguration;
    }
}
