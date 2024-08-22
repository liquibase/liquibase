package liquibase.analytics;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class SegmentBatch {
    private final List<SegmentTrackEvent> batch = new ArrayList<>();
    private final String writeKey;
    private final Map<String, ?> context;

    public static SegmentBatch fromLiquibaseEvent(Event event) {
        SegmentBatch segmentBatch = new SegmentBatch(TelemetryConfiguration.WRITE_KEY.getCurrentValue(), null);
        segmentBatch.getBatch().add(SegmentTrackEvent.fromLiquibaseEvent(event));
        return segmentBatch;
    }
}
