package liquibase.telemetry;

import liquibase.Scope;
import liquibase.telemetry.configuration.SegmentTelemetryConfiguration;
import liquibase.telemetry.configuration.TelemetryConfiguration;
import liquibase.telemetry.configuration.TelemetryConfigurationFactory;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

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
        addEventsToBatch(event, segmentBatch);
        return segmentBatch;
    }

    private static void addEventsToBatch(Event event, SegmentBatch segmentBatch) {
        List<Event> childEvents = event.getChildEvents();
        segmentBatch.getBatch().add(SegmentTrackEvent.fromLiquibaseEvent(event));
        if (CollectionUtils.isNotEmpty(childEvents)) {
            // if there are children, recursively add all of the children events to the batch
            for (Event childEvent : childEvents) {
                addEventsToBatch(childEvent, segmentBatch);
            }
        }
    }
}
