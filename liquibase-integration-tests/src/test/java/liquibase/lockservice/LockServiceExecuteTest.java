package liquibase.lockservice;

import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.executor.*;
import liquibase.exception.DatabaseException;
import liquibase.exception.LockException;
import liquibase.test.TestContext;
import org.junit.After;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.Before;

import java.sql.SQLException;
import java.sql.Statement;

public class LockServiceExecuteTest {

    @Before
    public void setUp() throws DatabaseException, LockException {
        ExecutorService.getInstance().reset();
        LockServiceFactory.getInstance().resetAll();

        fixupLockTables();
    }

    private void fixupLockTables() throws DatabaseException, LockException {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database.getConnection() != null) {
                Statement statement = null;
                try {
                    statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
                    try {
                        statement.execute("drop table " + database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName()));
                    } catch (Exception e) {
                        //ok
                    }
                    try {
                        statement.execute("drop table " + database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName()));
                    } catch (Exception e) {
                        //ok
                    }
                    statement.close();
                    database.commit();
                } catch (SQLException e) {
                    throw new DatabaseException(e);
                }
            }
        }
    }

    @After
    public void tearDown() throws LockException, DatabaseException {
        LockServiceFactory.getInstance().resetAll();

        fixupLockTables();
    }


    @Test
    public void nothing() {

    }

    //todo: failing on build server: re-enable
//    @Test
//    public void waitForLock_twoConnections() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
//            public void performTest(Database database) throws Exception {
////                if (database instanceof H2Database) {
////                    return;
////                }
//
//                String url = DatabaseTestContext.getInstance().getTestUrl(database);
//                System.out.println(url);
//                DatabaseConnection connection2 = DatabaseTestContext.getInstance().openDatabaseConnection(url);
//                Database database2 = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection2);
//
//                assertTrue(LockService.getInstance(database).acquireLock());
//                assertTrue(LockService.getInstance(database).hasChangeLogLock());
//                assertFalse(LockService.getInstance(database2).hasChangeLogLock());
//
//                assertFalse(LockService.getInstance(database2).acquireLock());
//                assertFalse(LockService.getInstance(database2).acquireLock());
//
//                LockService.getInstance(database).releaseLock();
//                assertTrue(LockService.getInstance(database2).acquireLock());
//
//            }
//        });
//    }
//
//    @Test
//    public void waitForLock_severalAquireLocksCalled() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
//            public void performTest(Database database) throws Exception {
//                assertTrue(LockService.getInstance(database).acquireLock());
//                assertTrue(LockService.getInstance(database).acquireLock());
//                assertTrue(LockService.getInstance(database).acquireLock());
//                assertTrue(LockService.getInstance(database).acquireLock());
//            }
//        });
//    }
//
//    @Test
//    public void waitForLock_emptyDatabase() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new DatabaseTest() {
//
//                    public void performTest(Database database) throws Exception {
//                        Executor executor = ExecutorService.getInstance().getExecutor(database);
//                        try {
//                            LockService.getInstance(database).resetAll();
//
//                            executor.execute(new DropTableStatement(null, database.getDatabaseChangeLogTableName(), false), new ArrayList<SqlVisitor>());
//                        } catch (DatabaseException e) {
//                            ; //must not be there
//                        }
//                        try {
//                            executor.execute(new DropTableStatement(null, database.getDatabaseChangeLogLockTableName(), false), new ArrayList<SqlVisitor>());
//                        } catch (DatabaseException e) {
//                            ; //must not be there
//                        }
//
//                        database.commit();
//
//                        LockService lockManager = LockService.getInstance(database);
//                        lockManager.waitForLock();
//                        lockManager.waitForLock();
//                    }
//
//                });
//    }
//
//    @Test
//    public void waitForLock_loggingDatabase() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new DatabaseTest() {
//
//                    public void performTest(Database database) throws Exception {
//
//                        LockService.getInstance(database).resetAll();
//
//                        Executor executor = ExecutorService.getInstance().getExecutor(database);
//                        try {
//                            executor.execute(new DropTableStatement(null, database.getDatabaseChangeLogTableName(), false), new ArrayList<SqlVisitor>());
//                        } catch (DatabaseException e) {
//                            ; //must not be there
//                        }
//                        try {
//                            executor.execute(new DropTableStatement(null, database.getDatabaseChangeLogLockTableName(), false), new ArrayList<SqlVisitor>());
//                        } catch (DatabaseException e) {
//                            ; //must not be there
//                        }
//
//                        database.commit();
//
//                        ExecutorService.getInstance().setExecutor(database, (new LoggingExecutor(ExecutorService.getInstance().getExecutor(database), new StringWriter(), database)));
//
//                        LockService lockManager = LockService.getInstance(database);
//                        lockManager.waitForLock();
//                    }
//
//                });
//    }
//
//    @Test
//    public void waitForLock_loggingThenExecute() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new DatabaseTest() {
//
//                    public void performTest(Database database) throws Exception {
//
//                        LockService.getInstance(database).resetAll();
//
//                        try {
//                            ExecutorService.getInstance().getExecutor(database).execute(new DropTableStatement(null, database.getDatabaseChangeLogTableName(), false), new ArrayList<SqlVisitor>());
//                        } catch (DatabaseException e) {
//                            ; //must not be there
//                        }
//                        try {
//                            ExecutorService.getInstance().getExecutor(database).execute(new DropTableStatement(null, database.getDatabaseChangeLogLockTableName(), false), new ArrayList<SqlVisitor>());
//                        } catch (DatabaseException e) {
//                            ; //must not be there
//                        }
//
//                        database.commit();
//
////                        Database clearDatabase = database.getClass().newInstance();
////                        clearDatabase.setConnection(database.getConnection());
//
//                        Executor originalTemplate = ExecutorService.getInstance().getExecutor(database);
//                        ExecutorService.getInstance().setExecutor(database, new LoggingExecutor(originalTemplate, new StringWriter(), database));
//
//                        LockService lockManager = LockService.getInstance(database);
//                        lockManager.waitForLock();
//
//                        ExecutorService.getInstance().setExecutor(database, originalTemplate);
//                        lockManager.waitForLock();
//
////                        database.getWriteExecutor().execute(database.getSelectChangeLogLockSQL());
//                    }
//
//                });
//    }

}
