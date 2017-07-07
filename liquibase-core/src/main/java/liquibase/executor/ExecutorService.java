package liquibase.executor;

import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutorService {

    private static ExecutorService instance = new ExecutorService();

    private Map<Database, Executor> executors = new ConcurrentHashMap<>();

    private ExecutorService() {
    }

    public static ExecutorService getInstance() {
        return instance;
    }

    public Executor getExecutor(Database database) {
        if (!executors.containsKey(database)) {
            try {
                Executor executor = (Executor) ServiceLocator.getInstance().newInstance(Executor.class);
                executor.setDatabase(database);
                executors.put(database, executor);
            } catch (Exception e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }
        return executors.get(database);
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
