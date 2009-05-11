package liquibase.lock;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.template.Executor;
import liquibase.database.template.JdbcOutputTemplate;
import liquibase.exception.JDBCException;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.DropTableStatement;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.TestContext;
import org.junit.After;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.io.StringWriter;
import java.sql.Connection;
import java.util.ArrayList;

public class LockServiceExecuteTest {

    @After
    public void tearDown() {
        LockService.resetAll();
    }

    @Test
    public void waitForLock_twoConnections() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
//                if (database instanceof H2Database) {
//                    return;
//                }
                
                String url = TestContext.getInstance().getTestUrl(database);
                System.out.println(url);
                Connection connection2 = TestContext.getInstance().openDatabaseConnection(url);
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
                        try {
                            LockService.getInstance(database).reset();

                            database.execute(new DropTableStatement(null, database.getDatabaseChangeLogTableName(), false), new ArrayList<SqlVisitor>());
                        } catch (JDBCException e) {
                            ; //must not be there
                        }
                        try {
                            database.execute(new DropTableStatement(null, database.getDatabaseChangeLogLockTableName(), false), new ArrayList<SqlVisitor>());
                        } catch (JDBCException e) {
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
                        ;

                        try {
                            database.execute(new DropTableStatement(null, database.getDatabaseChangeLogTableName(), false), new ArrayList<SqlVisitor>());
                        } catch (JDBCException e) {
                            ; //must not be there
                        }
                        try {
                            database.execute(new DropTableStatement(null, database.getDatabaseChangeLogLockTableName(), false), new ArrayList<SqlVisitor>());
                        } catch (JDBCException e) {
                            ; //must not be there
                        }

                        database.commit();

                        database.setJdbcTemplate(new JdbcOutputTemplate(new StringWriter(), database));

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
                            database.execute(new DropTableStatement(null, database.getDatabaseChangeLogTableName(), false), new ArrayList<SqlVisitor>());
                        } catch (JDBCException e) {
                            ; //must not be there
                        }
                        try {
                            database.execute(new DropTableStatement(null, database.getDatabaseChangeLogLockTableName(), false), new ArrayList<SqlVisitor>());
                        } catch (JDBCException e) {
                            ; //must not be there
                        }

                        database.commit();

//                        Database clearDatabase = database.getClass().newInstance();
//                        clearDatabase.setConnection(database.getConnection());

                        Executor originalTemplate = database.getExecutor();
                        database.setJdbcTemplate(new JdbcOutputTemplate(new StringWriter(), database));

                        LockService lockManager = LockService.getInstance(database);
                        lockManager.waitForLock();

                        database.setJdbcTemplate(originalTemplate);
                        lockManager.waitForLock();

//                        database.getExecutor().execute(database.getSelectChangeLogLockSQL());
                    }

                });
    }

}
