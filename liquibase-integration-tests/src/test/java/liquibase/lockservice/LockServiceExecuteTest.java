package liquibase.lockservice;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.DatabaseConnection;
import liquibase.database.JdbcConnection;
import liquibase.executor.LoggingExecutor;
import liquibase.executor.Executor;
import liquibase.executor.*;
import liquibase.exception.DatabaseException;
import liquibase.exception.LockException;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.core.DropTableStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.TestContext;
import liquibase.test.DatabaseTestContext;
import liquibase.sqlgenerator.core.InsertGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.logging.LogFactory;
import org.junit.After;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.Before;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.TreeSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class LockServiceExecuteTest {

    @Before
    public void setUp() throws DatabaseException, LockException {
        ExecutorService.getInstance().reset();
        LockService.resetAll();

        fixupLockTables();
    }

    private void fixupLockTables() throws DatabaseException, LockException {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database.getConnection() != null) {
                Statement statement = null;
                try {
                    statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
                    try {
                        statement.execute("drop table " + database.escapeTableName(database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName()));
                    } catch (Exception e) {
                        //ok
                    }
                    try {
                        statement.execute("drop table " + database.escapeTableName(database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName()));
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
        LockService.resetAll();

        fixupLockTables();
    }

    @Test
    public void waitForLock_twoConnections() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
//                if (database instanceof H2Database) {
//                    return;
//                }

                String url = DatabaseTestContext.getInstance().getTestUrl(database);
                System.out.println(url);
                DatabaseConnection connection2 = DatabaseTestContext.getInstance().openDatabaseConnection(url);
                Database database2 = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection2);

                assertTrue(LockService.getInstance(database).acquireLock());
                assertTrue(LockService.getInstance(database).hasChangeLogLock());
                assertFalse(LockService.getInstance(database2).hasChangeLogLock());

                assertFalse(LockService.getInstance(database2).acquireLock());
                assertFalse(LockService.getInstance(database2).acquireLock());

                LockService.getInstance(database).releaseLock();
                assertTrue(LockService.getInstance(database2).acquireLock());

            }
        });
    }

    @Test
    public void waitForLock_severalAquireLocksCalled() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                assertTrue(LockService.getInstance(database).acquireLock());
                assertTrue(LockService.getInstance(database).acquireLock());
                assertTrue(LockService.getInstance(database).acquireLock());
                assertTrue(LockService.getInstance(database).acquireLock());
            }
        });
    }

    @Test
    public void waitForLock_emptyDatabase() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new DatabaseTest() {

                    public void performTest(Database database) throws Exception {
                        Executor executor = ExecutorService.getInstance().getExecutor(database);
                        try {
                            LockService.getInstance(database).reset();

                            executor.execute(new DropTableStatement(null, database.getDatabaseChangeLogTableName(), false), new ArrayList<SqlVisitor>());
                        } catch (DatabaseException e) {
                            ; //must not be there
                        }
                        try {
                            executor.execute(new DropTableStatement(null, database.getDatabaseChangeLogLockTableName(), false), new ArrayList<SqlVisitor>());
                        } catch (DatabaseException e) {
                            ; //must not be there
                        }

                        database.commit();

                        LockService lockManager = LockService.getInstance(database);
                        lockManager.waitForLock();
                        lockManager.waitForLock();
                    }

                });
    }

    @Test
    public void waitForLock_loggingDatabase() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new DatabaseTest() {

                    public void performTest(Database database) throws Exception {

                        LockService.getInstance(database).reset();

                        Executor executor = ExecutorService.getInstance().getExecutor(database);
                        try {
                            executor.execute(new DropTableStatement(null, database.getDatabaseChangeLogTableName(), false), new ArrayList<SqlVisitor>());
                        } catch (DatabaseException e) {
                            ; //must not be there
                        }
                        try {
                            executor.execute(new DropTableStatement(null, database.getDatabaseChangeLogLockTableName(), false), new ArrayList<SqlVisitor>());
                        } catch (DatabaseException e) {
                            ; //must not be there
                        }

                        database.commit();

                        ExecutorService.getInstance().setExecutor(database, (new LoggingExecutor(ExecutorService.getInstance().getExecutor(database), new StringWriter(), database)));

                        LockService lockManager = LockService.getInstance(database);
                        lockManager.waitForLock();
                    }

                });
    }

    @Test
    public void waitForLock_loggingThenExecute() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new DatabaseTest() {

                    public void performTest(Database database) throws Exception {

                        LockService.getInstance(database).reset();

                        try {
                            ExecutorService.getInstance().getExecutor(database).execute(new DropTableStatement(null, database.getDatabaseChangeLogTableName(), false), new ArrayList<SqlVisitor>());
                        } catch (DatabaseException e) {
                            ; //must not be there
                        }
                        try {
                            ExecutorService.getInstance().getExecutor(database).execute(new DropTableStatement(null, database.getDatabaseChangeLogLockTableName(), false), new ArrayList<SqlVisitor>());
                        } catch (DatabaseException e) {
                            ; //must not be there
                        }

                        database.commit();

//                        Database clearDatabase = database.getClass().newInstance();
//                        clearDatabase.setConnection(database.getConnection());

                        Executor originalTemplate = ExecutorService.getInstance().getExecutor(database);
                        ExecutorService.getInstance().setExecutor(database, new LoggingExecutor(originalTemplate, new StringWriter(), database));

                        LockService lockManager = LockService.getInstance(database);
                        lockManager.waitForLock();

                        ExecutorService.getInstance().setExecutor(database, originalTemplate);
                        lockManager.waitForLock();

//                        database.getWriteExecutor().execute(database.getSelectChangeLogLockSQL());
                    }

                });
    }

}
