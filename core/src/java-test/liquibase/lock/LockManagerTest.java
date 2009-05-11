package liquibase.lock;

import liquibase.database.Database;
import liquibase.database.MySQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.exception.LockException;
import liquibase.statement.LockDatabaseChangeLogStatement;
import liquibase.statement.SelectFromDatabaseChangeLogLockStatement;
import liquibase.statement.UnlockDatabaseChangeLogStatement;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Test;

import java.text.DateFormat;
import java.util.*;
import java.lang.reflect.Field;

@SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
public class LockManagerTest {

    @Test
    public void getInstance() {
        final Database oracle1 = new OracleDatabase() {
            @Override
            public boolean equals(Object o) {
                return o == this;
            }
        };
        final Database oracle2 = new OracleDatabase() {
            @Override
            public boolean equals(Object o) {
                return o == this;
            }

        };
        final Database mysql = new MySQLDatabase() {
            @Override
            public boolean equals(Object o) {
                return o == this;
            }
        };

        assertNotNull(LockManager.getInstance(oracle1));
        assertNotNull(LockManager.getInstance(oracle2));
        assertNotNull(LockManager.getInstance(mysql));

        assertTrue(LockManager.getInstance(oracle1) == LockManager.getInstance(oracle1));
        assertTrue(LockManager.getInstance(oracle2) == LockManager.getInstance(oracle2));
        assertTrue(LockManager.getInstance(mysql) == LockManager.getInstance(mysql));

        assertTrue(LockManager.getInstance(oracle1) != LockManager.getInstance(oracle2));
        assertTrue(LockManager.getInstance(oracle1) != LockManager.getInstance(mysql));
    }


    @Test
    public void aquireLock_hasLockAlready() throws Exception {
        Database database = createMock(Database.class);
        replay(database);

        LockManager lockManager = LockManager.getInstance(database);
        assertFalse(lockManager.hasChangeLogLock());

        Field field = lockManager.getClass().getDeclaredField("hasChangeLogLock");
        field.setAccessible(true);
        field.set(lockManager, true);

        assertTrue(lockManager.hasChangeLogLock());

        assertTrue(lockManager.acquireLock());
    }


    @Test
    public void acquireLock_tableExistsNotLocked() throws Exception {
        Database database = createMock(Database.class);

        database.checkDatabaseChangeLogLockTable();
        expectLastCall();

        expect(database.queryForObject(isA(SelectFromDatabaseChangeLogLockStatement.class), eq(Boolean.class), isA(ArrayList.class))).andReturn(false);

        database.comment("Lock Database");
        expectLastCall();

        expect(database.update(isA(LockDatabaseChangeLogStatement.class), isA(ArrayList.class))).andReturn(1);

        database.commit();
        expectLastCall();

        replay(database);

        LockManager manager = LockManager.getInstance(database);
        assertTrue(manager.acquireLock());

        verify(database);
    }

    @Test
    public void acquireLock_tableExistsIsLocked() throws Exception {
        Database database = createMock(Database.class);

        database.checkDatabaseChangeLogLockTable();
        expectLastCall();

        expect(database.queryForObject(isA(SelectFromDatabaseChangeLogLockStatement.class), eq(Boolean.class), isA(ArrayList.class))).andReturn(true);

        replay(database);

        LockManager manager = LockManager.getInstance(database);
        assertFalse(manager.acquireLock());

        verify(database);
    }

    @Test
    public void waitForLock_notLocked() throws Exception {
        Database database = createMock(Database.class);

        database.checkDatabaseChangeLogLockTable();
        expectLastCall();

        expect(database.queryForObject(isA(SelectFromDatabaseChangeLogLockStatement.class), eq(Boolean.class), isA(ArrayList.class))).andReturn(false);

        database.comment("Lock Database");
        expectLastCall();

        expect(database.update(isA(LockDatabaseChangeLogStatement.class), isA(ArrayList.class))).andReturn(1);

        database.commit();
        expectLastCall();

        replay(database);

        LockManager manager = LockManager.getInstance(database);
        manager.waitForLock();

        verify(database);
    }

    @Test
    public void waitForLock_lockedThenReleased() throws Exception {
        Database database = createMock(Database.class);

        database.checkDatabaseChangeLogLockTable();
        expectLastCall().anyTimes();

        expect(database.queryForObject(isA(SelectFromDatabaseChangeLogLockStatement.class), eq(Boolean.class), isA(ArrayList.class))).andReturn(true);
        expect(database.queryForObject(isA(SelectFromDatabaseChangeLogLockStatement.class), eq(Boolean.class), isA(ArrayList.class))).andReturn(true);
        expect(database.queryForObject(isA(SelectFromDatabaseChangeLogLockStatement.class), eq(Boolean.class), isA(ArrayList.class))).andReturn(true);
        expect(database.queryForObject(isA(SelectFromDatabaseChangeLogLockStatement.class), eq(Boolean.class), isA(ArrayList.class))).andReturn(false);

        database.comment("Lock Database");
        expectLastCall();

        expect(database.update(isA(LockDatabaseChangeLogStatement.class), isA(ArrayList.class))).andReturn(1);

        database.commit();
        expectLastCall();

        replay(database);

        LockManager manager = LockManager.getInstance(database);
        manager.setChangeLogLockRecheckTime(1);
        manager.waitForLock();

        verify(database);
    }

    @Test
    public void waitForLock_lockedThenTimeout() throws Exception {
        Database database = createMock(Database.class);

        database.checkDatabaseChangeLogLockTable();
        expectLastCall().anyTimes();

        expect(database.queryForObject(isA(SelectFromDatabaseChangeLogLockStatement.class), eq(Boolean.class), isA(ArrayList.class))).andReturn(true).anyTimes();
        expect(database.doesChangeLogLockTableExist()).andReturn(true);

        List<Map> resultList = new ArrayList<Map>();
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("ID", 1);
        result.put("LOCKED", true);
        Date lockDate = new Date();
        result.put("LOCKGRANTED", lockDate);
        result.put("LOCKEDBY", "Locker");
        resultList.add(result);

        expect(database.queryForList(isA(SelectFromDatabaseChangeLogLockStatement.class), isA(ArrayList.class))).andReturn(resultList);

        replay(database);

        LockManager manager = LockManager.getInstance(database);
        manager.setChangeLogLockWaitTime(10);
        manager.setChangeLogLockRecheckTime(5);

        try {
            manager.waitForLock();
            fail("Should have thrown exception");
        } catch (LockException e) {
            assertEquals("Could not acquire change log lock.  Currently locked by Locker since " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(lockDate), e.getMessage());
        }

        verify(database);
    }

    @Test
    public void releaseLock_tableExistsAndLocked() throws Exception {
        Database database = createMock(Database.class);

        expect(database.update(isA(UnlockDatabaseChangeLogStatement.class), isA(ArrayList.class))).andReturn(1);
        expect(database.doesChangeLogLockTableExist()).andReturn(true);
        database.commit();
        expectLastCall().atLeastOnce();

        database.comment("Release Database Lock");
        expectLastCall().anyTimes();

        replay(database);

        LockManager manager = LockManager.getInstance(database);
        manager.releaseLock();

        verify(database);
    }

    @Test
    public void listLocks_hasLocks() throws Exception {
        Database database = createMock(Database.class);

        database.checkDatabaseChangeLogLockTable();
        expectLastCall().anyTimes();

        expect(database.queryForObject(isA(SelectFromDatabaseChangeLogLockStatement.class), eq(Boolean.class), isA(ArrayList.class))).andReturn(true).anyTimes();
        expect(database.doesChangeLogLockTableExist()).andReturn(true);

        List<Map> resultList = new ArrayList<Map>();
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("ID", 1);
        result.put("LOCKED", true);
        Date lockDate = new Date();
        result.put("LOCKGRANTED", lockDate);
        result.put("LOCKEDBY", "Locker");
        resultList.add(result);

        expect(database.queryForList(isA(SelectFromDatabaseChangeLogLockStatement.class), isA(ArrayList.class))).andReturn(resultList);

        replay(database);

        LockManager manager = LockManager.getInstance(database);
        DatabaseChangeLogLock[] locks = manager.listLocks();
        assertEquals(1, locks.length);
        assertEquals(1, locks[0].getId());
        assertEquals("Locker", locks[0].getLockedBy());
        assertEquals(lockDate, locks[0].getLockGranted());

        verify(database);
    }

    @Test
    public void listLocks_tableExistsUnlocked() throws Exception {
        Database database = createMock(Database.class);

        database.checkDatabaseChangeLogLockTable();
        expectLastCall().anyTimes();

        expect(database.queryForObject(isA(SelectFromDatabaseChangeLogLockStatement.class), eq(Boolean.class), isA(ArrayList.class))).andReturn(true).anyTimes();
        expect(database.doesChangeLogLockTableExist()).andReturn(true);

        List<Map> resultList = new ArrayList<Map>();

        expect(database.queryForList(isA(SelectFromDatabaseChangeLogLockStatement.class), isA(ArrayList.class))).andReturn(resultList);

        replay(database);

        LockManager manager = LockManager.getInstance(database);
        DatabaseChangeLogLock[] locks = manager.listLocks();
        assertEquals(0, locks.length);

        verify(database);
    }

    @Test
    public void listLocks_tableDoesNotExists() throws Exception {
        Database database = createMock(Database.class);

        expect(database.doesChangeLogLockTableExist()).andReturn(false);

        replay(database);

        LockManager manager = LockManager.getInstance(database);
        DatabaseChangeLogLock[] locks = manager.listLocks();
        assertEquals(0, locks.length);

        verify(database);
    }

//    @Test
//    public void waitForLock_emptyDatabase() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new DatabaseTest() {
//
//                    public void performTest(Database database) throws Exception {
//                        try {
//                            LockManager.getInstance(database).reset();
//
//                            new Executor(database).execute(new DropTableStatement(null, database.getDatabaseChangeLogTableName(), false));
//                        } catch (JDBCException e) {
//                            ; //must not be there
//                        }
//                        try {
//                            new Executor(database).execute(new DropTableStatement(null, database.getDatabaseChangeLogLockTableName(), false));
//                        } catch (JDBCException e) {
//                            ; //must not be there
//                        }
//
//                        database.commit();
//
//                        LockManager lockManager = LockManager.getInstance(database);
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
//                        LockManager.getInstance(database).reset();
//                        ;
//
//                        try {
//                            new Executor(database).execute(new DropTableStatement(null, database.getDatabaseChangeLogTableName(), false));
//                        } catch (JDBCException e) {
//                            ; //must not be there
//                        }
//                        try {
//                            new Executor(database).execute(new DropTableStatement(null, database.getDatabaseChangeLogLockTableName(), false));
//                        } catch (JDBCException e) {
//                            ; //must not be there
//                        }
//
//                        database.commit();
//
//                        database.setJdbcTemplate(new JdbcOutputTemplate(new StringWriter(), database));
//
//                        LockManager lockManager = LockManager.getInstance(database);
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
//                        LockManager.getInstance(database).reset();
//
//                        try {
//                            new Executor(database).execute(new DropTableStatement(null, database.getDatabaseChangeLogTableName(), false));
//                        } catch (JDBCException e) {
//                            ; //must not be there
//                        }
//                        try {
//                            new Executor(database).execute(new DropTableStatement(null, database.getDatabaseChangeLogLockTableName(), false));
//                        } catch (JDBCException e) {
//                            ; //must not be there
//                        }
//
//                        database.commit();
//
////                        Database clearDatabase = database.getClass().newInstance();
////                        clearDatabase.setConnection(database.getConnection());
//
//                        Executor originalTemplate = database.getExecutor();
//                        database.setJdbcTemplate(new JdbcOutputTemplate(new StringWriter(), database));
//
//                        LockManager lockManager = LockManager.getInstance(database);
//                        lockManager.waitForLock();
//
//                        database.setJdbcTemplate(originalTemplate);
//                        lockManager.waitForLock();
//
////                        database.getExecutor().execute(database.getSelectChangeLogLockSQL());
//                    }
//
//                });
//    }
}
