package liquibase.executor;

import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutorService {

    private static ExecutorService instance = new ExecutorService();

    private List<Executor> registry = new ArrayList<>();
    private Map<String, Executor> executorsByName = new ConcurrentHashMap<>();

    private Map<Database, Executor> executors = new ConcurrentHashMap<>();

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

    public void register(Executor executor) {
        registry.add(0, executor);
    }

    public static ExecutorService getInstance() {
        return instance;
    }

    public Executor getExecutor(String executorName) throws UnexpectedLiquibaseException {
        if (executorsByName.containsKey(executorName)) {
            return executorsByName.get(executorName);
        }
        SortedSet<Executor> foundExecutors = new TreeSet<>(new Comparator<Executor>() {
            @Override
            public int compare(Executor o1, Executor o2) {
                return -1 * Integer.valueOf(o1.getPriority()).compareTo(o2.getPriority());
            }
        });

        for (Executor executor : registry) {
            if (executor.getName().equals(executorName)) {
                foundExecutors .add(executor);
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

            executorsByName.put(executorName, executor);
            return executor;
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public Executor getExecutor(Database database) {
        return executors.computeIfAbsent(database, db -> {
            try {
                Executor executor = (Executor) ServiceLocator.getInstance().newInstance(Executor.class);
                executor.setDatabase(db);
                return executor;
            } catch (Exception e) {
                throw new UnexpectedLiquibaseException(e);
            }
        });
    }

    public void setExecutor(Database database, Executor executor) {
        executors.put(database, executor);
    }

    public void clearExecutor(Database database) {
        executors.remove(database);
    }

    public void reset() {
        executors.clear();
    }
}
