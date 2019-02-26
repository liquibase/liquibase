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
