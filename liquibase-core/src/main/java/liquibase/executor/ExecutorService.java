package liquibase.executor;

import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.plugin.AbstractPluginFactory;
import liquibase.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutorService extends AbstractPluginFactory<Executor>  {

    private final Map<Key, Executor> executors = new ConcurrentHashMap<>();

    private ExecutorService() {
    }

    @Override
    protected Class<Executor> getPluginClass() {
        return Executor.class;
    }

    @Override
    protected int getPriority(Executor executor, Object... args) {
        String name = (String) args[0];
        Database database = (Database) args[1];
        if (name.equals(executor.getName()) && executor.supports(database)) {
            return executor.getPriority();
        } else {
            return Plugin.PRIORITY_NOT_APPLICABLE;
        }

    }

    private Key createKey(String executorName, Database database) {
        return new Key(executorName, database);
    }

    private Executor getExecutorValue(String executorName, Database database) throws UnexpectedLiquibaseException {
        final Executor plugin = getPlugin(executorName.toLowerCase(), database);
        try {
            return plugin.getClass().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves a named executor for the specified database.
     *
     * @param name     the name of the executor
     * @param database the database for which to retrieve the executor
     * @return the {@code Executor} associated with the given name and database
     * @throws UnexpectedLiquibaseException if there was an error retrieving the executor
     */
    public Executor getExecutor(final String name, final Database database) {
        return executors.computeIfAbsent(createKey(name, database), db -> {
            try {
                Executor executor = getExecutorValue(name, database);
                executor.setDatabase(database);
                return executor;
            } catch (Exception e) {
                throw new UnexpectedLiquibaseException(e);
            }
        });
    }

    /**
     * Checks if an executor exists for the given name and database.
     *
     * @param name     the name of the executor
     * @param database the database to which the executor is connected
     * @return {@code true} if an executor exists for the given name and database, {@code false} otherwise
     */
    public boolean executorExists(final String name, final Database database) {
        return executors.containsKey(createKey(name, database));
    }

    /**
     * Returns an {@code Executor} for the specified database and name.
     * This method is deprecated; please use {@link #getExecutor(String, Database)} instead.
     *
     * @param database the {@code Database} to execute the statements on
     * @return {@code Executor} for the specified database and name
     * @deprecated Please use {@link #getExecutor(String, Database) } instead.
     */
    public Executor getExecutor(Database database) {
        return getExecutor("jdbc", database);
    }

    /**
     * Sets the executor for the given database with the default name "jdbc". If an executor with the same name and database already exists,
     * it will be replaced by the new one.
     *
     * @param database the {@code Database} for which the executor is set
     * @param executor the {@code Executor} to set
     */
    public void setExecutor(Database database, Executor executor) {
        setExecutor("jdbc", database, executor);
    }

    public void setExecutor(String name, Database database, Executor executor) {
        executors.put(createKey(name, database), executor);
    }

    /**
     *
     * @deprecated   Please use clearExecutor(name, database)
     * @param        database
     *
     */
    public void clearExecutor(Database database) {
        clearExecutor("jdbc", database);
    }

    public void clearExecutor(String name, Database database) {
        executors.remove(createKey(name, database));
    }

    public void reset() {
        executors.clear();
    }

    private static class Key {
        private final String executorName;
        private final Database database;

        Key(String executorName, Database database) {
            this.executorName = normalizeExecutorName(executorName);
            this.database = database;
        }

        private String normalizeExecutorName(String executorName) {
            return executorName.toLowerCase();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equals(executorName, key.executorName)
                    && database == key.database; // equality by reference to be consistent with hashCode
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    executorName,
                    // this class already relied on identity hash code. It bypasses e.g. AbstractJdbcDatabase's hashCode
                    System.identityHashCode(database)
            );
        }

        @Override
        public String toString() {
            return "Key{" +
                    "executorName='" + executorName + '\'' +
                    ", database=" + database +
                    '}';
        }
    }
}
