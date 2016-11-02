package liquibase.lockservice;

import org.junit.After;
import org.junit.Before;

@SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
public class StandardLockServiceTest {

    private StandardLockService lockService;

    @Before
    public void before() {
        lockService = new StandardLockService();
        lockService.reset();
    }

    @After
    public void after() {
        lockService.reset();
    }

//    @Test
//    public void aquireLock_hasLockAlready() throws Exception {
//        Database database = createMock(Database.class);
//        replay(database);
//
//        lockService.setDatabase(database);
//        assertFalse(lockService.hasChangeLogLock());
//
//        Field field = lockService.getClass().getDeclaredField("hasChangeLogLock");
//        field.setAccessible(true);
//        field.set(lockService, true);
//
//        assertTrue(lockService.hasChangeLogLock());
//
//        assertTrue(lockService.acquireLock());
//    }


//    @Test
//    public void acquireLock_tableExistsNotLocked() throws Exception {
//        Database database = createMock(Database.class);
//        Executor executor = createMock(Executor.class);
//
////        database.checkDatabaseChangeLogLockTable();
////        expectLastCall();
//
//        database.rollback();
//        expectLastCall().anyTimes();
//
//        database.setCanCacheLiquibaseTableInfo(true);
//        expectLastCall();
//
//        expect(executor.queryForObject(isA(SelectFromDatabaseChangeLogLockStatement.class), eq(Boolean.class))).andReturn(false);
//
//        executor.comment("Lock Database");
//        expectLastCall();
//
//        expect(executor.update(isA(LockDatabaseChangeLogStatement.class))).andReturn(1);
//
//        database.commit();
//        expectLastCall();
//
//        replay(executor);
//        replay(database);
//        ExecutorService.getInstance().setExecutor(database, executor);
//
//        LockServiceImpl service = new LockServiceImpl();
//        service.setDatabase(database);
//        assertTrue(service.acquireLock());
//
//        verify(database);
//        verify(executor);
//    }

//    @Test
//    public void acquireLock_tableExistsIsLocked() throws Exception {
//        Database database = createMock(Database.class);
//        Executor executor = createMock(Executor.class);
//
////        database.checkDatabaseChangeLogLockTable();
////        expectLastCall();
//
//        database.rollback();
//        expectLastCall().anyTimes();
//
//        expect(executor.queryForObject(isA(SelectFromDatabaseChangeLogLockStatement.class), eq(Boolean.class))).andReturn(true);
//
//        replay(database);
//        replay(executor);
//        ExecutorService.getInstance().setExecutor(database, executor);
//
//        LockServiceImpl service = new LockServiceImpl();
//        service.setDatabase(database);
//        assertFalse(service.acquireLock());
//
//        verify(database);
//    }

//    @Test
//    public void waitForLock_notLocked() throws Exception {
//        Database database = createMock(Database.class);
//        Executor executor = createMock(Executor.class);
//
//
////        database.checkDatabaseChangeLogLockTable();
////        expectLastCall();
//
//        database.setCanCacheLiquibaseTableInfo(true);
//        expectLastCall();
//
//        database.rollback();
//        expectLastCall().anyTimes();
//
//        expect(executor.queryForObject(isA(SelectFromDatabaseChangeLogLockStatement.class), eq(Boolean.class))).andReturn(false);
//
//        expect(database.getDatabaseChangeLogLockTableName()).andReturn("DATABASECHANGELOGLOCK");
//        expect(database.getLiquibaseCatalogName()).andReturn("LQCAT");
//        expect(database.getLiquibaseSchemaName()).andReturn("LQSCHM");
//        database.setCanCacheLiquibaseTableInfo(true);
//        expectLastCall();
//
//        executor.comment("Lock Database");
//        expectLastCall();
//
//        expect(executor.update(isA(LockDatabaseChangeLogStatement.class))).andReturn(1);
//
//        database.commit();
//        expectLastCall();
//
//        replay(database);
//        replay(executor);
//        ExecutorService.getInstance().setExecutor(database, executor);
//
//        LockServiceImpl service = new LockServiceImpl();
//        service.setDatabase(database);
//        service.waitForLock();
//
//        verify(database);
//        verify(executor);
//    }

//    @Test
//    public void waitForLock_lockedThenReleased() throws Exception {
//        Database database = createMock(Database.class);
//        Executor executor = createMock(Executor.class);
//
////        database.checkDatabaseChangeLogLockTable();
////        expectLastCall().anyTimes();
//
//        database.setCanCacheLiquibaseTableInfo(true);
//        expectLastCall();
//
//        expect(executor.queryForObject(isA(SelectFromDatabaseChangeLogLockStatement.class), eq(Boolean.class))).andReturn(true);
//        expect(executor.queryForObject(isA(SelectFromDatabaseChangeLogLockStatement.class), eq(Boolean.class))).andReturn(true);
//        expect(executor.queryForObject(isA(SelectFromDatabaseChangeLogLockStatement.class), eq(Boolean.class))).andReturn(true);
//        expect(executor.queryForObject(isA(SelectFromDatabaseChangeLogLockStatement.class), eq(Boolean.class))).andReturn(false);
//
//        executor.comment("Lock Database");
//        expectLastCall();
//
//        database.rollback();
//        expectLastCall().anyTimes();
//
//        expect(executor.update(isA(LockDatabaseChangeLogStatement.class))).andReturn(1);
//
//        database.commit();
//        expectLastCall();
//
//        replay(database);
//        replay(executor);
//        ExecutorService.getInstance().setExecutor(database, executor);
//
//        LockServiceImpl service = new LockServiceImpl();
//        service.setDatabase(database);
//        service.setChangeLogLockRecheckTime(1);
//        service.waitForLock();
//
//        verify(database);
//        verify(executor);
//    }

//    @Test
//    public void waitForLock_lockedThenTimeout() throws Exception {
//        Database database = createMock(Database.class);
//        Executor executor = createMock(Executor.class);
//
////        database.checkDatabaseChangeLogLockTable();
////        expectLastCall().anyTimes();
//
////        expect(executor.queryForObject(isA(SelectFromDatabaseChangeLogLockStatement.class), eq(Boolean.class))).andReturn(true).anyTimes();
////        expect(database.hasDatabaseChangeLogLockTable()).andReturn(true);
//
//        List<Map<String, ?>> resultList = new ArrayList<Map<String, ?>>();
//        Map<String, Object> result = new HashMap<String, Object>();
//        result.put("ID", 1);
//        result.put("LOCKED", true);
//        Date lockDate = new Date();
//        result.put("LOCKGRANTED", lockDate);
//        result.put("LOCKEDBY", "Locker");
//        resultList.add(result);
//
//        expect(executor.queryForList(isA(SelectFromDatabaseChangeLogLockStatement.class))).andReturn(resultList);
//
//        database.rollback();
//        expectLastCall().anyTimes();
//
//        replay(database);
//        replay(executor);
//        ExecutorService.getInstance().setExecutor(database, executor);
//
//        LockServiceImpl service = new LockServiceImpl();
//        service.setDatabase(database);
//        service.setChangeLogLockWaitTime(10);
//        service.setChangeLogLockRecheckTime(5);
//
//        try {
//            service.waitForLock();
//            fail("Should have thrown exception");
//        } catch (LockException e) {
//            assertEquals("Could not acquire change log lock.  Currently locked by Locker since " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(lockDate), e.getMessage());
//        }
//
//        verify(database);
//    }

//    @Test
//    public void releaseLock_tableExistsAndLocked() throws Exception {
//        Database database = createMock(Database.class);
//        Executor executor = createMock(Executor.class);
//
//        expect(executor.update(isA(UnlockDatabaseChangeLogStatement.class))).andReturn(1);
////        expect(database.hasDatabaseChangeLogLockTable()).andReturn(true);
//        database.commit();
//        expectLastCall().atLeastOnce();
//
//        database.setCanCacheLiquibaseTableInfo(false);
//        expectLastCall();
//
//        executor.comment("Release Database Lock");
//        expectLastCall().anyTimes();
//
//        database.rollback();
//        expectLastCall().anyTimes();
//
//        replay(database);
//        replay(executor);
//        ExecutorService.getInstance().setExecutor(database, executor);
//
//        LockServiceImpl service = new LockServiceImpl();
//        service.setDatabase(database);
//        service.releaseLock();
//
//        verify(database);
//    }

//    @Test
//    public void listLocks_hasLocks() throws Exception {
//        Database database = createMock(Database.class);
//        Executor executor = createMock(Executor.class);
//
////        database.checkDatabaseChangeLogLockTable();
////        expectLastCall().anyTimes();
//
////        expect(executor.queryForObject(isA(SelectFromDatabaseChangeLogLockStatement.class), eq(Boolean.class))).andReturn(true).anyTimes();
////        expect(database.hasDatabaseChangeLogLockTable()).andReturn(true);
//
//        List<Map<String, ?>> resultList = new ArrayList<Map<String, ?>>();
//        Map<String, Object> result = new HashMap<String, Object>();
//        result.put("ID", 1);
//        result.put("LOCKED", true);
//        Date lockDate = new Date();
//        result.put("LOCKGRANTED", lockDate);
//        result.put("LOCKEDBY", "Locker");
//        resultList.add(result);
//
//        expect(executor.queryForList(isA(SelectFromDatabaseChangeLogLockStatement.class))).andReturn(resultList);
//
//        replay(database);
//        replay(executor);
//        ExecutorService.getInstance().setExecutor(database, executor);
//
//        LockServiceImpl service = new LockServiceImpl();
//        service.setDatabase(database);
//        DatabaseChangeLogLock[] locks = service.listLocks();
//        assertEquals(1, locks.length);
//        assertEquals(1, locks[0].getId());
//        assertEquals("Locker", locks[0].getLockedBy());
//        assertEquals(lockDate, locks[0].getLockGranted());
//
//        verify(database);
//    }

//    @Test
//    public void listLocks_tableExistsUnlocked() throws Exception {
//        Database database = createMock(Database.class);
//        Executor executor = createMock(Executor.class);
//
////        database.checkDatabaseChangeLogLockTable();
////        expectLastCall().anyTimes();
//
////        expect(executor.queryForObject(isA(SelectFromDatabaseChangeLogLockStatement.class), eq(Boolean.class))).andReturn(true).anyTimes();
////        expect(database.hasDatabaseChangeLogLockTable()).andReturn(true);
//
//        List<Map<String, ?>> resultList = new ArrayList<Map<String, ?>>();
//
//        expect(executor.queryForList(isA(SelectFromDatabaseChangeLogLockStatement.class))).andReturn(resultList);
//
//        replay(database);
//        replay(executor);
//        ExecutorService.getInstance().setExecutor(database, executor);
//
//        LockServiceImpl service = new LockServiceImpl();
//        service.setDatabase(database);
//        DatabaseChangeLogLock[] locks = service.listLocks();
//        assertEquals(0, locks.length);
//
//        verify(database);
//    }

//    @Test
//    public void listLocks_tableDoesNotExists() throws Exception {
//        Database database = createMock(Database.class);
//
////        expect(database.hasDatabaseChangeLogLockTable()).andReturn(false);
//
//        replay(database);
//
//        LockServiceImpl service = new LockServiceImpl();
//        service.setDatabase(database);
//        DatabaseChangeLogLock[] locks = service.listLocks();
//        assertEquals(0, locks.length);
//
//        verify(database);
//    }

    public void testNothing() {

    }

}
