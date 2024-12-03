package liquibase.analytics;

import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class AnalyticsTrackEvent {
    private final String type = "track";
    /**
     * Unique identifier for the user in your database.
     * A userId or an anonymousId is required. See the Identities docs for more details.
     * https://segment.com/docs/connections/spec/identify#identities
     */
    private final String userId;
    /**
     * A pseudo-unique substitute for a User ID, for cases when you don’t have an absolutely unique identifier.
     * A userId or an anonymousId is required.
     * See the Identities docs for more details: https://segment.com/docs/connections/spec/identify#identities
     */
    private final String anonymousId;
    /**
     * Name of the action that a user has performed. See the Event field docs for more details.
     * https://segment.com/docs/connections/spec/track#event
     */
    private final String event = "liquibase-command-executed";
    /**
     * Free-form dictionary of properties of the event, like revenue.
     * See the Properties docs for a list of reserved property names.
     * https://segment.com/docs/connections/spec/track#properties
     */
    private final Map<String, ?> properties;
    /**
     * Dictionary of extra information that provides useful context about a message,
     * but is not directly related to the API call like ip address or locale See the Context field docs for more details.
     * https://segment.com/docs/connections/spec/common#context
     */
    private final Map<String, ?> context;
    private final String messageId = UUID.randomUUID().toString();

    public static AnalyticsTrackEvent fromLiquibaseEvent(Event event, String userId) {
        AnalyticsTrackEvent analyticsTrackEvent = new AnalyticsTrackEvent(
                userId,
                new AnonymousSeed().generateId(),
                event.getPropertiesAsMap(),
                null
        );
        return analyticsTrackEvent;
    }
}
