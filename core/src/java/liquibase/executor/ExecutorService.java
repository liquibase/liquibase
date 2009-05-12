package liquibase.executor;

import liquibase.database.Database;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutorService {

    private static ExecutorService instance = new ExecutorService();

    private Map<Database, ReadExecutor> readExecutors = new ConcurrentHashMap<Database, ReadExecutor>();
    private Map<Database, WriteExecutor> writeExecutors = new ConcurrentHashMap<Database, WriteExecutor>();


    private ExecutorService() {
    }

    public static ExecutorService getInstance() {
        return instance;
    }

    public WriteExecutor getWriteExecutor(Database database) {
        if (!writeExecutors.containsKey(database)) {
            writeExecutors.put(database, new DefaultExecutor(database));
        }
        return writeExecutors.get(database);
    }

    public ReadExecutor getReadExecutor(Database database) {
        if (!readExecutors.containsKey(database)) {
            readExecutors.put(database, new DefaultExecutor(database));
        }
        return readExecutors.get(database);
    }

    public void setWriteExecutor(Database database, WriteExecutor writeExecutor) {
        writeExecutors.put(database, writeExecutor);
    }

    public void setReadExecutor(Database database, ReadExecutor readExecutor) {
        readExecutors.put(database, readExecutor);
    }
}
