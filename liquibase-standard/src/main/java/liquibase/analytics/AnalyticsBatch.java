package liquibase.analytics;

import liquibase.Scope;
import liquibase.analytics.configuration.AnalyticsConfiguration;
import liquibase.analytics.configuration.AnalyticsConfigurationFactory;
import liquibase.analytics.configuration.LiquibaseRemoteAnalyticsConfiguration;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The batch method lets you send a series of Identify, Group, Track, Page and Screen requests in a single batch,
 * saving on outbound requests.
 * See more info here: https://segment.com/docs/connections/sources/catalog/libraries/server/http-api/#batch
 */
@Data
public class AnalyticsBatch {
    /**
     * An array of Identify, Group, Track, Page and Screen method calls. Each call must have an type property with a
     * valid method name.
     */
    private final List<AnalyticsTrackEvent> batch = new ArrayList<>();
    /**
     * The write key is a unique identifier for each source. It lets Segment know which source is sending the data and
     * which destinations should receive that data.
     */
    private final String writeKey;
    /**
     * The same as Context for other calls, but it will be merged with any context inside each of the items in the batch.
     */
    private final Map<String, ?> context;

    @Deprecated
    public static AnalyticsBatch fromLiquibaseEvent(Event event, String userId) throws Exception {
        return fromLiquibaseEvent(Collections.singletonList(event), userId);
    }

    /**
     * Converts a Liquibase-specific event into an {@link AnalyticsBatch} that is expected by Segment.
     * <p>
     * This method processes the given Liquibase {@link Event}, converts it into a format that Segment
     * can handle, and wraps it into an {@link AnalyticsBatch}. If the event has child events, those
     * will be recursively added to the batch. The batch is constructed using the write key from the
     * {@link LiquibaseRemoteAnalyticsConfiguration}, if available.
     * </p>
     *
     * @param event  the Liquibase {@link Event} to be converted, which may contain child events
     * @param userId the ID of the user associated with this event, used to identify the event's source
     * @return an {@link AnalyticsBatch} object containing the converted event and any child events
     * @throws Exception if there is an error during event processing or configuration retrieval
     */
    public static AnalyticsBatch fromLiquibaseEvent(List<Event> events, String userId) throws Exception {
        AnalyticsConfigurationFactory analyticsConfigurationFactory = Scope.getCurrentScope().getSingleton(AnalyticsConfigurationFactory.class);
        AnalyticsConfiguration analyticsConfiguration = analyticsConfigurationFactory.getPlugin();
        String writeKey = null;
        if (analyticsConfiguration instanceof LiquibaseRemoteAnalyticsConfiguration) {
            writeKey = ((LiquibaseRemoteAnalyticsConfiguration) analyticsConfiguration).getWriteKey();
        }
        AnalyticsBatch analyticsBatch = new AnalyticsBatch(writeKey, null);
        for (Event event : events) {
            addEventsToBatch(event, analyticsBatch, userId);
        }
        return analyticsBatch;
    }

    /**
     * Recursively adds the given event and its child events (if any) to the provided {@link AnalyticsBatch}.
     * <p>
     * Each event is converted into an {@link AnalyticsTrackEvent} and added to the batch. If the event
     * contains any child events, they are recursively processed and added to the same batch.
     * </p>
     * @param event         the event to be added to the batch, along with any child events
     * @param analyticsBatch the batch to which the event and its children will be added
     * @param userId        the user ID associated with the event
     */
    private static void addEventsToBatch(Event event, AnalyticsBatch analyticsBatch, String userId) {
        List<Event> childEvents = event.getChildEvents();
        analyticsBatch.getBatch().add(AnalyticsTrackEvent.fromLiquibaseEvent(event, userId));
        if (CollectionUtils.isNotEmpty(childEvents)) {
            // If there are child events, recursively add them to the batch
            for (Event childEvent : childEvents) {
                addEventsToBatch(childEvent, analyticsBatch, userId);
            }
        }
    }
}
