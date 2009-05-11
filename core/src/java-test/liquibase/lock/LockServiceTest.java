package liquibase.lock;

import liquibase.database.Database;
import liquibase.database.MySQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.database.template.JdbcOutputTemplate;
import liquibase.database.template.Executor;
import liquibase.exception.LockException;
import liquibase.exception.JDBCException;
import liquibase.statement.LockDatabaseChangeLogStatement;
import liquibase.statement.SelectFromDatabaseChangeLogLockStatement;
import liquibase.statement.UnlockDatabaseChangeLogStatement;
import liquibase.statement.DropTableStatement;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.sql.visitor.SqlVisitor;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.After;

import java.text.DateFormat;
import java.util.*;
import java.lang.reflect.Field;
import java.io.StringWriter;

@SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
public class LockServiceTest {

    @After
    public void after() {
        LockService.resetAll();
    }

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

        assertNotNull(LockService.getInstance(oracle1));
        assertNotNull(LockService.getInstance(oracle2));
        assertNotNull(LockService.getInstance(mysql));

        assertTrue(LockService.getInstance(oracle1) == LockService.getInstance(oracle1));
        assertTrue(LockService.getInstance(oracle2) == LockService.getInstance(oracle2));
        assertTrue(LockService.getInstance(mysql) == LockService.getInstance(mysql));

        assertTrue(LockService.getInstance(oracle1) != LockService.getInstance(oracle2));
        assertTrue(LockService.getInstance(oracle1) != LockService.getInstance(mysql));
    }


    @Test
    public void aquireLock_hasLockAlready() throws Exception {
        Database database = createMock(Database.class);
        replay(database);

        LockService lockService = LockService.getInstance(database);
        assertFalse(lockService.hasChangeLogLock());

        Field field = lockService.getClass().getDeclaredField("hasChangeLogLock");
        field.setAccessible(true);
        field.set(lockService, true);

        assertTrue(lockService.hasChangeLogLock());

        assertTrue(lockService.acquireLock());
    }


    @Test
    public void acquireLock_tableExistsNotLocked() throws Exception {
        Database database = createMock(Database.class);

        database.checkDatabaseChangeLogLockTable();
        expectLastCall();

        database.rollback();
        expectLastCall().anyTimes();

        expect(database.queryForObject(isA(SelectFromDatabaseChangeLogLockStatement.class), eq(Boolean.class), isA(ArrayList.class))).andReturn(false);

        database.comment("Lock Database");
        expectLastCall();

        expect(database.update(isA(LockDatabaseChangeLogStatement.class), isA(ArrayList.class))).andReturn(1);

        database.commit();
        expectLastCall();

        replay(database);

        LockService service = LockService.getInstance(database);
        assertTrue(service.acquireLock());

        verify(database);
    }

    @Test
    public void acquireLock_tableExistsIsLocked() throws Exception {
        Database database = createMock(Database.class);

        database.checkDatabaseChangeLogLockTable();
        expectLastCall();

        database.rollback();
        expectLastCall().anyTimes();

        expect(database.queryForObject(isA(SelectFromDatabaseChangeLogLockStatement.class), eq(Boolean.class), isA(ArrayList.class))).andReturn(true);

        replay(database);

        LockService service = LockService.getInstance(database);
        assertFalse(service.acquireLock());

        verify(database);
    }

    @Test
    public void waitForLock_notLocked() throws Exception {
        Database database = createMock(Database.class);

        database.checkDatabaseChangeLogLockTable();
        expectLastCall();

        database.rollback();
        expectLastCall().anyTimes();

        expect(database.queryForObject(isA(SelectFromDatabaseChangeLogLockStatement.class), eq(Boolean.class), isA(ArrayList.class))).andReturn(false);

        database.comment("Lock Database");
        expectLastCall();

        expect(database.update(isA(LockDatabaseChangeLogStatement.class), isA(ArrayList.class))).andReturn(1);

        database.commit();
        expectLastCall();

        replay(database);

        LockService service = LockService.getInstance(database);
        service.waitForLock();

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

        database.rollback();
        expectLastCall().anyTimes();

        expect(database.update(isA(LockDatabaseChangeLogStatement.class), isA(ArrayList.class))).andReturn(1);

        database.commit();
        expectLastCall();

        replay(database);

        LockService service = LockService.getInstance(database);
        service.setChangeLogLockRecheckTime(1);
        service.waitForLock();

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

        database.rollback();
        expectLastCall().anyTimes();

        replay(database);

        LockService service = LockService.getInstance(database);
        service.setChangeLogLockWaitTime(10);
        service.setChangeLogLockRecheckTime(5);

        try {
            service.waitForLock();
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

        database.rollback();
        expectLastCall().anyTimes();
        
        replay(database);

        LockService service = LockService.getInstance(database);
        service.releaseLock();

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

        LockService service = LockService.getInstance(database);
        DatabaseChangeLogLock[] locks = service.listLocks();
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

        LockService service = LockService.getInstance(database);
        DatabaseChangeLogLock[] locks = service.listLocks();
        assertEquals(0, locks.length);

        verify(database);
    }

    @Test
    public void listLocks_tableDoesNotExists() throws Exception {
        Database database = createMock(Database.class);

        expect(database.doesChangeLogLockTableExist()).andReturn(false);

        replay(database);

        LockService service = LockService.getInstance(database);
        DatabaseChangeLogLock[] locks = service.listLocks();
        assertEquals(0, locks.length);

        verify(database);
    }
}
