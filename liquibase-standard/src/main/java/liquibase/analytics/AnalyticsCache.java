package liquibase.analytics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class stores analytics events and sends the events when the cached events exceeds the preset limit of cached
 * events. This allows a reduction in API calls because events are batched.
 */
public class AnalyticsCache {

    private final int maxCacheDepth = 2;
    private final List<Event> cachedEvents = new ArrayList<Event>(maxCacheDepth);
    private final AtomicBoolean addedShutdownHook = new AtomicBoolean(false);

    public void addEvent(Event event) {
        if (!addedShutdownHook.getAndSet(true)) {
            Thread haltedHook = new Thread(() -> {
                System.out.println("Sending events during shutdown hook");
                sendEvents();
            });
            Runtime.getRuntime().addShutdownHook(haltedHook);
        }
        // todo set timestamp on event, rather than using Segment server time (because events will be sent later)
        cachedEvents.add(event);
        if (cachedEvents.size() >= maxCacheDepth) {
            sendEvents();
        }
    }

    private void flush() {
        sendEvents();
    }

    private void sendEvents() {
        synchronized (cachedEvents) {
            // todo flatten and send events here
            System.out.println("Sending " + cachedEvents.size() + " events");
            cachedEvents.clear();
        }
    }
}
