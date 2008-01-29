package liquibase.lock;

import liquibase.DatabaseChangeLogLock;
import liquibase.database.Database;
import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.sql.UpdateStatement;
import liquibase.database.template.JdbcTemplate;
import liquibase.exception.LockException;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.*;

public class LockHandlerTest  {

    @Test
    public void acquireLock_tableExistsNotLocked() throws Exception {
        Database database = createMock(Database.class);
        JdbcTemplate template = createMock(JdbcTemplate.class);
        RawSqlStatement selectLockStatement = new RawSqlStatement("SELECT LOCK");

        expect(database.getJdbcTemplate()).andReturn(template).anyTimes();
        expect(database.doesChangeLogLockTableExist()).andReturn(true);
        expect(database.getSelectChangeLogLockSQL()).andReturn(selectLockStatement);
        expect(database.getDatabaseChangeLogLockTableName()).andReturn("LOCK_TABLE").anyTimes();
        expect(database.getDatabaseChangeLogTableName()).andReturn("DATABASECHANGELOG").anyTimes();
        expect(database.getDefaultSchemaName()).andReturn(null).anyTimes();

        database.commit();
        expectLastCall();

        expect(template.queryForObject(selectLockStatement, Boolean.class)).andReturn(Boolean.FALSE);
        expect(template.update(isA(UpdateStatement.class))).andReturn(1);
        template.comment("Lock Database");
        expectLastCall();

        replay(database);
        replay(template);

        LockHandler handler = LockHandler.getInstance(database);
        handler.acquireLock();

        verify(database);
        verify(template);
    }

    @Test
    public void acquireLock_tableNotExists() throws Exception {
        Database database = createMock(Database.class);
        JdbcTemplate template = createMock(JdbcTemplate.class);

        expect(database.getJdbcTemplate()).andReturn(template).anyTimes();
        expect(database.doesChangeLogLockTableExist()).andReturn(false);
        expectLastCall();


        replay(database);
        replay(template);

        LockHandler handler = LockHandler.getInstance(database);
        try {
            handler.acquireLock();
            fail("did not throw exception");
        } catch (LockException e) {
            assertEquals("Could not acquire lock, table does not exist", e.getMessage());
        }

        verify(database);
        verify(template);
    }

     @Test
    public void acquireLock_tableExistsIsLocked() throws Exception {
        Database database = createMock(Database.class);
        JdbcTemplate template = createMock(JdbcTemplate.class);
        RawSqlStatement selectLockStatement = new RawSqlStatement("SELECT LOCK");

        expect(database.getJdbcTemplate()).andReturn(template).anyTimes();
        expect(database.doesChangeLogLockTableExist()).andReturn(true);
        expect(database.getSelectChangeLogLockSQL()).andReturn(selectLockStatement);
        expectLastCall();

        expect(template.queryForObject(selectLockStatement, Boolean.class)).andReturn(Boolean.TRUE);

        replay(database);
        replay(template);

        LockHandler handler = LockHandler.getInstance(database);
        handler.acquireLock();

        verify(database);
        verify(template);
    }

    @Test
    public void releaseLock_tableExistsAndLocked() throws Exception {
        Database database = createMock(Database.class);
        JdbcTemplate template = createMock(JdbcTemplate.class);

        expect(database.getJdbcTemplate()).andReturn(template).anyTimes();
        expect(database.doesChangeLogLockTableExist()).andReturn(true);
        expect(database.getDatabaseChangeLogTableName()).andReturn("DATABASECHANGELOG").anyTimes();
        database.commit();
        expectLastCall().atLeastOnce();
        expect(database.getDefaultSchemaName()).andReturn(null).anyTimes();        

        expect(database.getDatabaseChangeLogLockTableName()).andReturn("lock_table").anyTimes();
        expectLastCall();

        expect(template.update(isA(UpdateStatement.class))).andReturn(1);
        template.comment("Lock Database");
        expectLastCall().anyTimes();
        template.comment("Release Database Lock");
        expectLastCall().anyTimes();

        replay(database);
        replay(template);

        LockHandler handler = LockHandler.getInstance(database);
        handler.releaseLock();

        verify(database);
        verify(template);

    }

    @Test
    public void listLocks_tableExistsWithLock() throws Exception {
        Database database = createMock(Database.class);
        JdbcTemplate template = createMock(JdbcTemplate.class);

        expect(database.getJdbcTemplate()).andReturn(template).anyTimes();
        expect(database.doesChangeLogLockTableExist()).andReturn(true);
        expect(database.getDatabaseChangeLogLockTableName()).andReturn("lock_table");
        expect(database.getDefaultSchemaName()).andReturn("default_schema");
        expect(database.escapeTableName("default_schema", "lock_table")).andReturn("default_schema.lock_table");
        expectLastCall();

        List<Map> locksList = new ArrayList<Map>();
        Map lock = new HashMap();
        lock.put("ID", 1);
        lock.put("LOCKGRANTED", new Date());
        lock.put("LOCKED", Boolean.TRUE);
        lock.put("LOCKEDBY", "127.0.0.1");
        locksList.add(lock);

        expect(template.queryForList(isA(SqlStatement.class))).andReturn(locksList);

        replay(database);
        replay(template);

        LockHandler handler = LockHandler.getInstance(database);
        DatabaseChangeLogLock[] locks = handler.listLocks();
        assertEquals(1, locks.length);

        verify(database);
        verify(template);

    }

    @Test
    public void listLocks_tableExistsUnlocked() throws Exception {
        Database database = createMock(Database.class);
        JdbcTemplate template = createMock(JdbcTemplate.class);

        expect(database.getJdbcTemplate()).andReturn(template).anyTimes();
        expect(database.doesChangeLogLockTableExist()).andReturn(true);
        expect(database.getDatabaseChangeLogLockTableName()).andReturn("lock_table");
        expectLastCall();
        expect(database.getDefaultSchemaName()).andReturn("default_schema");
        expect(database.escapeTableName("default_schema", "lock_table")).andReturn("default_schema.lock_table");

        List<Map> locksList = new ArrayList<Map>();
        Map lock = new HashMap();
        lock.put("ID", 1);
        lock.put("LOCKGRANTED", new Date());
        lock.put("LOCKED", Boolean.FALSE);
        lock.put("LOCKEDBY", "127.0.0.1");
        locksList.add(lock);

        expect(template.queryForList(isA(SqlStatement.class))).andReturn(locksList);

        replay(database);
        replay(template);

        LockHandler handler = LockHandler.getInstance(database);
        DatabaseChangeLogLock[] locks = handler.listLocks();
        assertEquals(0, locks.length);

        verify(database);
        verify(template);

    }

    @Test
    public void listLocks_tableExistsNoLocks() throws Exception {
        Database database = createMock(Database.class);
        JdbcTemplate template = createMock(JdbcTemplate.class);

        expect(database.getJdbcTemplate()).andReturn(template).anyTimes();
        expect(database.doesChangeLogLockTableExist()).andReturn(true);
        expect(database.getDefaultSchemaName()).andReturn("default_schema");
        expect(database.getDatabaseChangeLogLockTableName()).andReturn("lock_table");
        expectLastCall();
        expect(database.escapeTableName("default_schema", "lock_table")).andReturn("default_schema.lock_table");

        List<Map> locksList = new ArrayList<Map>();

        expect(template.queryForList(isA(SqlStatement.class))).andReturn(locksList);

        replay(database);
        replay(template);

        LockHandler handler = LockHandler.getInstance(database);
        DatabaseChangeLogLock[] locks = handler.listLocks();
        assertEquals(0, locks.length);

        verify(database);
        verify(template);

    }

    @Test
    public void listLocks_tableDoesNotExists() throws Exception {
        Database database = createMock(Database.class);

        expect(database.doesChangeLogLockTableExist()).andReturn(false);

        replay(database);

        LockHandler handler = LockHandler.getInstance(database);
        DatabaseChangeLogLock[] locks = handler.listLocks();
        assertEquals(0, locks.length);

        verify(database);
    }
}
