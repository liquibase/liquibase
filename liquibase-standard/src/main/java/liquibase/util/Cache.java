package liquibase.util;

import lombok.Getter;

import java.util.concurrent.Callable;

/**
 * A wrapper around a method which is expected to be called multiple times. This class orchestrates ensuring that the
 * method is called once and storing the result value for subsequent executions, with support for time-to-live (TTL).
 *
 * Some of the logic in this class is borrowed from the Guava implementation of ExpiringMemoizingSupplier:
 * https://github.com/google/guava/blob/cc2c5d3d6623fe66a969c29fcb422bf02fb57a1f/guava/src/com/google/common/base/Suppliers.java#L286-L346
 */
public class Cache<T> {
    private transient Object lock = new Object();
    /**
     * The actual value, which will be cached.
     */
    transient volatile private T value;
    /**
     * The function that will be called to generate the value and stored in the cache.
     */
    private final Callable<T> generator;
    /**
     * True if the generator function has been called already.
     */
    @Getter
    private boolean generated = false;
    /**
     * If true, and the generator throws an exception, this is considered "generated" and future calls to the get method
     * will rethrow the same exception without recalling the generator.
     */
    private boolean exceptionIsPermitted = false;
    /**
     * The exception thrown by the generator, if any.
     */
    private Exception exception;

    /**
     * The time (in milliseconds) after which the cached value will expire. If set to any value less than 1, cached values will never expire.
     */
    transient volatile private long timeToLiveMillis;

    /**
     * The timestamp of the last successful cache generation, in milliseconds since epoch.
     */
    private long lastGeneratedTime;

    public Cache(Callable<T> generator) {
        this(generator, false);
    }

    public Cache(Callable<T> generator, boolean exceptionIsPermitted) {
        this(generator, exceptionIsPermitted, 0);
    }

    public Cache(Callable<T> generator, boolean exceptionIsPermitted, long timeToLiveMillis) {
        this.generator = generator;
        this.exceptionIsPermitted = exceptionIsPermitted;
        this.timeToLiveMillis = timeToLiveMillis;
    }

    public T get() throws Exception {
        if (!generated || isExpired()) {
            synchronized (lock) {
                try {
                    value = generator.call();
                    lastGeneratedTime = System.currentTimeMillis();
                    exception = null; // Reset exception if the call is successful
                } catch (Exception e) {
                    if (exceptionIsPermitted) {
                        exception = e;
                        generated = true;
                    }
                    throw e;
                }
                generated = true;
            }
        }
        if (exception != null) {
            throw exception;
        }
        return value;
    }

    /**
     * Clears the cache and resets its state.
     */
    public void clearCache() {
        generated = false;
        lastGeneratedTime = 0;
        exception = null;
    }

    /**
     * Checks if the cache has expired based on the time-to-live (TTL).
     *
     * @return true if the cache is expired, false otherwise.
     */
    private boolean isExpired() {
        return timeToLiveMillis > 0 && (System.currentTimeMillis() - lastGeneratedTime) > timeToLiveMillis;
    }
}
