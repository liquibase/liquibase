package liquibase.analytics;

import liquibase.Scope;
import liquibase.analytics.configuration.SegmentTelemetryConfiguration;
import liquibase.analytics.configuration.TelemetryConfiguration;
import liquibase.analytics.configuration.TelemetryConfigurationFactory;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class SegmentBatch {
    private final List<SegmentTrackEvent> batch = new ArrayList<>();
    private final String writeKey;
    private final Map<String, ?> context;

    public static SegmentBatch fromLiquibaseEvent(Event event) throws Exception {
        TelemetryConfigurationFactory telemetryConfigurationFactory = Scope.getCurrentScope().getSingleton(TelemetryConfigurationFactory.class);
        TelemetryConfiguration telemetryConfiguration = telemetryConfigurationFactory.getPlugin();
        String writeKey = null;
        if (telemetryConfiguration instanceof SegmentTelemetryConfiguration) {
            writeKey = ((SegmentTelemetryConfiguration) telemetryConfiguration).getWriteKey();
        }
        SegmentBatch segmentBatch = new SegmentBatch(writeKey, null);
        segmentBatch.getBatch().add(SegmentTrackEvent.fromLiquibaseEvent(event));
        return segmentBatch;
    }
}
