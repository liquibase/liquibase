package liquibase.logging;

/**
 * LoggerContexts are used for tracking "nested diagnostic context" as well as tracking progress.
 * It is up to the @{link {@link LoggerFactory} implementations to support them as they can.
 *
 * Implements {@link AutoCloseable} so they can be used nicely in a "try with resources" statement.
 */
public interface LoggerContext extends AutoCloseable {

    /**
     * Closes this LoggerContext and pops it off the LogFactory's context stack.
     */
    @Override
    void close();

    /**
     * Mark that some additional progress has been completed, like by adding a "." to output.
     * Most non-UI loggers will not implement this.
     */
    void showMoreProgress();

    /**
     * Mark that some progress within the context is the given percent complete.
     * Most non-UI loggers will not implement this.
     */
    void showMoreProgress(int percentComplete);
}
