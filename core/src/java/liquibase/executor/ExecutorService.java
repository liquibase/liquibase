package liquibase.executor;

import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.plugin.ClassPathScanner;

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
            try {
                WriteExecutor writeExecutor = (WriteExecutor) ClassPathScanner.getInstance().getClasses(WriteExecutor.class)[0].newInstance();
                writeExecutor.setDatabase(database);
                writeExecutors.put(database, writeExecutor);
            } catch (Exception e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }
        return writeExecutors.get(database);
    }

    public ReadExecutor getReadExecutor(Database database) {
        if (!readExecutors.containsKey(database)) {
            try {
                ReadExecutor readExecutor = (ReadExecutor) ClassPathScanner.getInstance().getClasses(ReadExecutor.class)[0].newInstance();
                readExecutor.setDatabase(database);
                readExecutors.put(database, readExecutor);
            } catch (Exception e) {
                throw new UnexpectedLiquibaseException(e);
            }
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
