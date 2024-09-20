package liquibase.analytics;

import liquibase.Scope;
import liquibase.analytics.configuration.SegmentAnalyticsConfiguration;
import liquibase.analytics.configuration.AnalyticsConfiguration;
import liquibase.analytics.configuration.AnalyticsConfigurationFactory;
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
        AnalyticsConfigurationFactory analyticsConfigurationFactory = Scope.getCurrentScope().getSingleton(AnalyticsConfigurationFactory.class);
        AnalyticsConfiguration analyticsConfiguration = analyticsConfigurationFactory.getPlugin();
        String writeKey = null;
        if (analyticsConfiguration instanceof SegmentAnalyticsConfiguration) {
            writeKey = ((SegmentAnalyticsConfiguration) analyticsConfiguration).getWriteKey();
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
