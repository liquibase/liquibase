package liquibase.analytics;

public class NoOpAnalyticsListener implements UsageAnalyticsListener {
    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public void handleEvent(Event event) {
        // purposefully do nothing
    }
}