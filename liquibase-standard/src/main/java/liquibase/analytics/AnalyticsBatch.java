package liquibase.analytics;

import liquibase.Scope;
import liquibase.analytics.configuration.LiquibaseRemoteAnalyticsConfiguration;
import liquibase.analytics.configuration.AnalyticsConfiguration;
import liquibase.analytics.configuration.AnalyticsConfigurationFactory;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class AnalyticsBatch {
    private final List<AnalyticsTrackEvent> batch = new ArrayList<>();
    private final String writeKey;
    private final Map<String, ?> context;

    public static AnalyticsBatch fromLiquibaseEvent(Event event, String userId) throws Exception {
        AnalyticsConfigurationFactory analyticsConfigurationFactory = Scope.getCurrentScope().getSingleton(AnalyticsConfigurationFactory.class);
        AnalyticsConfiguration analyticsConfiguration = analyticsConfigurationFactory.getPlugin();
        String writeKey = null;
        if (analyticsConfiguration instanceof LiquibaseRemoteAnalyticsConfiguration) {
            writeKey = ((LiquibaseRemoteAnalyticsConfiguration) analyticsConfiguration).getWriteKey();
        }
        AnalyticsBatch analyticsBatch = new AnalyticsBatch(writeKey, null);
        addEventsToBatch(event, analyticsBatch, userId);
        return analyticsBatch;
    }

    private static void addEventsToBatch(Event event, AnalyticsBatch analyticsBatch, String userId) {
        List<Event> childEvents = event.getChildEvents();
        analyticsBatch.getBatch().add(AnalyticsTrackEvent.fromLiquibaseEvent(event, userId));
        if (CollectionUtils.isNotEmpty(childEvents)) {
            // if there are children, recursively add all of the children events to the batch
            for (Event childEvent : childEvents) {
                addEventsToBatch(childEvent, analyticsBatch, userId);
            }
        }
    }
}
