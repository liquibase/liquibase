package liquibase.executor;

import liquibase.database.Database;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutorService {
    private static Map<Database, Executor> instances = new ConcurrentHashMap<Database, Executor>();

    public static Executor getExecutor(Database database) {
        if (!instances.containsKey(database)) {
            instances.put(database, new Executor(database));
        }
        return instances.get(database);
    }

    public static void setExecutor(Database database, Executor executor) {
        instances.put(database, executor);
    }
}
