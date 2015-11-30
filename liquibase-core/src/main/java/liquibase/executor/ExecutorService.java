package liquibase.executor;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.AbstractServiceFactory;
import liquibase.servicelocator.ServiceLocator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutorService extends AbstractServiceFactory<Executor> {

    private Map<Database, Executor> executors = new ConcurrentHashMap<Database, Executor>();


    protected ExecutorService(Scope scope) {
        super(scope);
    }

    @Override
    protected Class<Executor> getServiceClass() {
        return Executor.class;
    }

    @Override
    protected int getPriority(Executor obj, Scope scope, Object... args) {
        return obj.getPriority(scope);
    }

    public Executor getExecutor(Scope scope) {
        Database database = scope.getDatabase();
        if (database == null) {
            throw new UnexpectedLiquibaseException("No database set");
        }

        if (!executors.containsKey(database)) {
            try {
                Executor executor = getService(scope);
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
