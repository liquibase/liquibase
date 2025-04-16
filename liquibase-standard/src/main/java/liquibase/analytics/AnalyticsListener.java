package liquibase.analytics;

import liquibase.plugin.Plugin;

/**
 * The {@code UsageAnalyticsListener} interface extends the {@code Plugin} interface
 * and is designed for handling events related to usage analytics. Implementations
 * of this interface should define how specific events are handled.
 */
public interface AnalyticsListener extends Plugin {

    int getPriority();

    /**
     * Handles the specified event. Implementations of this method should define
     * the logic for processing the event that is passed as a parameter.
     *
     * @param event the event to be handled.
     */
    void handleEvent(Event event) throws Exception;

    boolean isEnabled();
}