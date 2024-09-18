package liquibase.analytics;

import liquibase.Scope;
import liquibase.analytics.configuration.SegmentAnalyticsConfiguration;
import liquibase.analytics.configuration.AnalyticsConfiguration;
import liquibase.analytics.configuration.AnalyticsConfigurationFactory;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class SegmentBatch {
    private final List<SegmentTrackEvent> batch = new ArrayList<>();
    private final String writeKey;
    private final Map<String, ?> context;

    public static SegmentBatch fromLiquibaseEvent(Event event, String licenseIssuedTo) throws Exception {
        AnalyticsConfigurationFactory analyticsConfigurationFactory = Scope.getCurrentScope().getSingleton(AnalyticsConfigurationFactory.class);
        AnalyticsConfiguration analyticsConfiguration = analyticsConfigurationFactory.getPlugin();
        String writeKey = null;
        if (analyticsConfiguration instanceof SegmentAnalyticsConfiguration) {
            writeKey = ((SegmentAnalyticsConfiguration) analyticsConfiguration).getWriteKey();
        }
        SegmentBatch segmentBatch = new SegmentBatch(writeKey, null);
        segmentBatch.getBatch().add(SegmentTrackEvent.fromLiquibaseEvent(event, licenseIssuedTo));
        return segmentBatch;
    }
}
