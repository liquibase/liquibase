package liquibase.util;

import lombok.Getter;

import java.util.concurrent.Callable;

/**
 * A wrapper around a method which is expected to be called multiple times. This class orchestrates ensuring that the
 * method is called once and storing the result value for subsequent executions.
 */
public class Cache<T> {
    /**
     * The actual value, which will be cached.
     */
    private T value;
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

    public Cache(Callable<T> generator) {
        this.generator = generator;
    }

    public Cache(Callable<T> generator, boolean exceptionIsPermitted) {
        this.generator = generator;
        this.exceptionIsPermitted = exceptionIsPermitted;
    }

    public T get() throws Exception {
        if (!generated) {
            try {
                value = generator.call();
            } catch (Exception e) {
                if (exceptionIsPermitted) {
                    exception = e;
                    generated = true;
                }
                throw e;
            }
            generated = true;
        }
        if (exception != null) {
            throw exception;
        }
        return value;
    }

    public void clearCache() {
        generated = false;
    }
}
