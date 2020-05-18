package liquibase.executor;

import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;

public class ExecutorService {

    private static ExecutorService instance = new ExecutorService();

    private List<Executor> registry = new ArrayList<>();

    private Map<String, Executor> executors = new HashMap<>();

    private ExecutorService() {
        Class<? extends Executor>[] classes;
        try {
            classes = ServiceLocator.getInstance().findClasses(Executor.class);

            for (Class<? extends Executor> clazz : classes) {
                register(clazz.getConstructor().newInstance());
            }

        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
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
        SortedSet<Executor> foundExecutors = new TreeSet<>(new Comparator<Executor>() {
            @Override
            public int compare(Executor o1, Executor o2) {
                return -1 * Integer.valueOf(o1.getPriority()).compareTo(o2.getPriority());
            }
        });

        for (Executor executor : registry) {
            if (executor.getName().toLowerCase().equals(executorName.toLowerCase())) {
                foundExecutors.add(executor);
            }
        }
        if (foundExecutors .isEmpty()) {
            throw new UnexpectedLiquibaseException("Cannot find Executor for " +  executorName);
        }

        try {
            Executor exampleService = foundExecutors .iterator().next();
            Class<? extends Executor> aClass = exampleService.getClass();
            Executor executor;
            try {
                aClass.getConstructor();
                executor = aClass.getConstructor().newInstance();
            } catch (NoSuchMethodException e) {
                // must have been manually added to the registry and so already configured.
                executor = exampleService;
            }

            return executor;
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    private void register(Executor executor) {
        registry.add(0, executor);
    }

    public static ExecutorService getInstance() {
        return instance;
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
                Executor executor = getExecutorValue(name, database); //(Executor) ServiceLocator.getInstance().newInstance(Executor.class);
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
