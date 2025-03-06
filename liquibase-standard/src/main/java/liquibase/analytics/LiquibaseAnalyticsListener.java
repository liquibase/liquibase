package liquibase.analytics;

import liquibase.Scope;
import liquibase.analytics.configuration.AnalyticsArgs;
import liquibase.analytics.configuration.LiquibaseRemoteAnalyticsConfiguration;
import lombok.NoArgsConstructor;

@NoArgsConstructor
/**
 * This class implements the {@link AnalyticsListener} interface and is responsible for handling
 * Liquibase analytics events. It sends anonymous usage data to a configured Liquibase analytics
 * endpoint for reporting purposes. The class ensures that analytics are only sent if enabled via
 * {@link AnalyticsArgs}.
 *
 * <p>The listener also manages user-specific data, such as the licensee information. It logs the
 * request and response details using the logging level configured through {@link AnalyticsArgs#LOG_LEVEL}.
 *
 * <p>Important considerations include obtaining user identification outside of threads to avoid
 * inconsistencies caused by {@link Scope}'s ThreadLocal storage. Additionally, it respects the
 * timeout configured by the {@link LiquibaseRemoteAnalyticsConfiguration#getTimeoutMillis()} setting.
 */
public class LiquibaseAnalyticsListener implements AnalyticsListener {

    // todo obtain this cache from scope, and set the cache depth limit in the constructor, so that spring can set something like 10, and we can limit it to 0 here (since CLI usage should immediately send the evnets)
    private final AnalyticsCache analyticsCache = new AnalyticsCache();

    @Override
    public int getPriority() {
        boolean analyticsEnabled = false;
        try {
            analyticsEnabled = AnalyticsArgs.isAnalyticsEnabled();
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(AnalyticsListener.class).log(AnalyticsArgs.LOG_LEVEL.getCurrentValue(), "Failed to determine if analytics is enabled", e);
        }
        if (analyticsEnabled) {
            return PRIORITY_SPECIALIZED;
        } else {
            return PRIORITY_NOT_APPLICABLE;
        }
    }

    @Override
    public void handleEvent(Event event) throws Exception {
        analyticsCache.addEvent(event);

    }
}
