package liquibase.extension.testing.testsystem.wrapper;

/**
 * Wraps the external database used by {@link liquibase.extension.testing.testsystem.DatabaseTestSystem}
 * so that HOW the database is interacted with is independent of WHAT we do with that connection.
 * For example, the same setup logic can be applied regardless of whether the wrapped database is accessed via {@link JdbcDatabaseWrapper} or {@link DockerDatabaseWrapper}.
 */
public abstract class DatabaseWrapper {

    /**
     * Start the database if possible and ensure it can be connected to.
     * If the database is managed externally, just ensure it can be connected to.
     */
    public abstract void start() throws Exception;


    /**
     * Stop the database if possible.
     * If the database is managed externally, do not actually stop it.
     */
    public abstract void stop() throws Exception;

    public abstract String getUsername();

    public abstract String getUrl();

    /**
     * Describes the configuration of this wrapper. Used in outputting to user how this connection is configured.
     */
    public abstract String describe();
}
