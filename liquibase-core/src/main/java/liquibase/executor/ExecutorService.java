package liquibase.executor;

import liquibase.Scope;
import liquibase.change.ChangeMetaData;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.plugin.AbstractPluginFactory;
import liquibase.plugin.Plugin;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutorService extends AbstractPluginFactory<Executor>  {

    private Map<String, Executor> executors = new ConcurrentHashMap<>();

    private ExecutorService() {
    }

    @Override
    protected Class<Executor> getPluginClass() {
        return Executor.class;
    }

    @Override
    protected int getPriority(Executor executor, Object... args) {
        String name = (String) args[0];
        if (name.equals(executor.getName())) {
            return executor.getPriority();
        } else {
            return Plugin.PRIORITY_NOT_APPLICABLE;
        }

    }

    private String createKey(String executorName, Database database) {
        String key = executorName.toLowerCase() + "#" + System.identityHashCode(database);
        return key;
    }

    private Executor getExecutorValue(String executorName, Database database) throws UnexpectedLiquibaseException {
        String key = createKey(executorName, database);
        if (executors.containsKey(key)) {
            return executors.get(key);
        }

        final Executor plugin = getPlugin(executorName.toLowerCase(), database);
        try {
            return plugin.getClass().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * Retrieve a named executor for the specified database
     *
     * @param   name
     * @param   database
     * @return  Executor
     *
     */
    public Executor getExecutor(final String name, final Database database) {
        Executor returnExecutor = executors.computeIfAbsent(createKey(name, database), db -> {
            try {
                Executor executor = getExecutorValue(name, database);
                executor.setDatabase(database);
                return executor;
            } catch (Exception e) {
                throw new UnexpectedLiquibaseException(e);
            }
        });
        return returnExecutor;
    }

    /**
     *
     * Return true if there is an existing Executor for this name/database
     *
     */
    public boolean executorExists(final String name, final Database database) {
        return executors.containsKey(createKey(name, database));
    }

    /**
     *
     * @deprecated  Please use getExecutor(name, database) instead
     * @param       database
     * @return      Executor
     *
     */
    public Executor getExecutor(Database database) {
        return getExecutor("jdbc", database);
    }

    /**
     *
     * @deprecated      Please use setExecutor(name, database, executor)
     * @param           database
     * @param           executor
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
}
