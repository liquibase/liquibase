package liquibase.test;

import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.MigrationFailedException;
import liquibase.executor.ExecutorService;
import liquibase.executor.jvm.JdbcExecutor;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import org.junit.ComparisonFailure;

import java.util.Set;

public class DatabaseTestTemplate {
    public void testOnAvailableDatabases(DatabaseTest test) throws Exception {
        test(test, DatabaseTestContext.getInstance().getAvailableDatabases());
    }

    public void testOnAllDatabases(DatabaseTest test) throws Exception {
        test(test, TestContext.getInstance().getAllDatabases());
    }

    private void test(DatabaseTest test, Set<Database> databasesToTestOn) throws Exception {
        for (Database database : databasesToTestOn) {
            if (database instanceof SQLiteDatabase) {
                continue; //todo: find how to get tests to run correctly on SQLite
            }
            JdbcExecutor writeExecutor = new JdbcExecutor();
            writeExecutor.setDatabase(database);
            ExecutorService.getInstance().setExecutor(database, writeExecutor);
            LockService lockService = LockServiceFactory.getInstance().getLockService(database);
            lockService.reset();
            if (database.getConnection() != null) {
                lockService.forceReleaseLock();
            }

            try {
                test.performTest(database);
            } catch (ComparisonFailure e) {
                String newMessage = "Database Test Failure on " + database;
                if (e.getMessage() != null) {
                    newMessage += ": " + e.getMessage();
                }

                ComparisonFailure newError = new ComparisonFailure(newMessage, e.getExpected(), e.getActual());
                newError.setStackTrace(e.getStackTrace());
                throw newError;
            } catch (AssertionError | MigrationFailedException e) {
                e.printStackTrace();
                String newMessage = "Database Test Failure on " + database;
                if (e.getMessage() != null) {
                    newMessage += ": " + e.getMessage();
                }

                AssertionError newError = new AssertionError(newMessage);
                newError.setStackTrace(e.getStackTrace());
                throw newError;
            } catch (Exception e) {
                e.printStackTrace();
                String newMessage = "Database Test Exception on " + database;
                if (e.getMessage() != null) {
                    newMessage += ": " + e.getMessage();
                }
                
                Exception newError = e.getClass().getConstructor(String.class).newInstance(newMessage);
                if (e.getCause() == null) {
                    newError.setStackTrace(e.getStackTrace());
                } else {
                    newError.setStackTrace(e.getCause().getStackTrace());                    
                }
                throw newError;
            } finally {
                if ((database.getConnection() != null) && !database.getAutoCommitMode()) {
                    database.rollback();
                }
            }
        }
    }
}
