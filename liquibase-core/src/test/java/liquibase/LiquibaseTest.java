package liquibase;

import liquibase.changelog.ChangeLogIterator;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ContextChangeSetFilter;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.changelog.filter.IgnoreChangeSetFilter;
import liquibase.changelog.filter.LabelChangeSetFilter;
import liquibase.changelog.filter.ShouldRunChangeSetFilter;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.sdk.database.MockDatabase;
import liquibase.exception.ChangeLogParseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ResourceAccessor;
import liquibase.test.Assert;
import liquibase.sdk.resource.MockResourceAccessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static liquibase.test.Assert.assertListsEqual;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LiquibaseTest {
    private MockResourceAccessor mockResourceAccessor;
    private Database mockDatabase;
    private LockServiceFactory mockLockServiceFactory;
    private LockService mockLockService;

    private ChangeLogParserFactory mockChangeLogParserFactory;
    private ChangeLogParser mockChangeLogParser;
    private DatabaseChangeLog mockChangeLog;
    private ChangeLogIterator mockChangeLogIterator;

    private Logger mockLogger;

//    private TestLiquibase testLiquibase;
//    private DatabaseConnection connectionForConstructor;

    @Before
    public void before() throws Exception {
//        if (connectionForConstructor != null) {
//            reset(connectionForConstructor);
//        }
//        connectionForConstructor = createMock(DatabaseConnection.class);
//        connectionForConstructor.setAutoCommit(false);
//        expectLastCall().atLeastOnce();
//
//        DatabaseMetaData metaData = createMock(DatabaseMetaData.class);
//        expect(metaData.getDatabaseProductName()).andReturn("Oracle");
//        replay(metaData);
//
////        expect(connectionForConstructor.getMetaData()).andReturn(metaData);
//        replay(connectionForConstructor);
//
//        testLiquibase = new TestLiquibase();
        mockResourceAccessor = new MockResourceAccessor();
        mockDatabase = mock(Database.class);
        mockLockService = mock(LockService.class);
        mockLockServiceFactory = mock(LockServiceFactory.class);
        mockChangeLogParserFactory = mock(ChangeLogParserFactory.class);
        mockChangeLogParser = mock(ChangeLogParser.class);
        mockChangeLog = mock(DatabaseChangeLog.class);
        mockChangeLogIterator = mock(ChangeLogIterator.class);

        mockLogger = mock(Logger.class);

        LockServiceFactory.setInstance(mockLockServiceFactory);
        when(mockLockServiceFactory.getLockService(any(Database.class))).thenReturn(mockLockService);

        ChangeLogParserFactory.setInstance(mockChangeLogParserFactory);
        when(mockChangeLogParserFactory.getParser(anyString(), Mockito.isA(ResourceAccessor.class))).thenReturn(mockChangeLogParser);
        when(mockChangeLogParser.parse(anyString(), any(ChangeLogParameters.class), Mockito.isA(ResourceAccessor.class))).thenReturn(mockChangeLog);

        LogFactory.setInstance(new LogFactory() {
            @Override
            public Logger getLog(String name) {
                return mockLogger;
            }
        });
    }

    @After
    public void after() {
        verifyNoMoreInteractions(mockLockService, mockChangeLogParser, mockChangeLog, mockChangeLogIterator); //for no other interactions of normal use objects. Not automatically checking mockDatabase and the *Factory mocks
        Mockito.reset(mockDatabase, mockLockServiceFactory, mockLockService, mockChangeLogParserFactory, mockChangeLogParser, mockChangeLog, mockChangeLogIterator);
        LockServiceFactory.reset();
        ChangeLogParserFactory.reset();
        LogFactory.reset();
    }

    @Test
    public void constructor() throws Exception {
        LogFactory.reset(); //going to test log setup
        MockResourceAccessor resourceAccessor = this.mockResourceAccessor;
        MockDatabase database = new MockDatabase();

        Liquibase liquibase = new Liquibase("com/example/test.xml", resourceAccessor, database);

        assertNotNull(liquibase.getLog());

        assertEquals("com/example/test.xml", liquibase.getChangeLogFile());


        assertSame(resourceAccessor, liquibase.getResourceAccessor());

        assertNotNull(liquibase.getChangeLogParameters());
        assertEquals("Standard database changelog parameters were not set", "DATABASECHANGELOGLOCK", liquibase.getChangeLogParameters().getValue("database.databaseChangeLogLockTableName", null));

        assertSame(database, liquibase.getDatabase());
    }

    @Test
    public void constructor_changelogPathsStandardize() throws Exception {
        Liquibase liquibase = new Liquibase("path\\with\\windows\\separators.xml", mockResourceAccessor, new MockDatabase());
        assertEquals("path/with/windows/separators.xml", liquibase.getChangeLogFile());

        liquibase = new Liquibase("path/with/unix/separators.xml", mockResourceAccessor, new MockDatabase());
        assertEquals("path/with/unix/separators.xml", liquibase.getChangeLogFile());

        liquibase = new Liquibase("/absolute/path/remains.xml", mockResourceAccessor, new MockDatabase());
        assertEquals("/absolute/path/remains.xml", liquibase.getChangeLogFile());
    }

    @Test
    public void constructor_createDatabaseInstanceFromConnection() throws LiquibaseException {
        DatabaseConnection databaseConnection = mock(DatabaseConnection.class);
        Database database = mockDatabase;

        try {
            DatabaseFactory.setInstance(mock(DatabaseFactory.class));
            when(DatabaseFactory.getInstance().findCorrectDatabaseImplementation(databaseConnection)).thenReturn(database);

            Liquibase liquibase = new Liquibase("com/example/test.xml", mockResourceAccessor, databaseConnection);
            assertSame("Liquibase constructor passing connection did not find the correct database implementation", database, liquibase.getDatabase());

        } finally {
            DatabaseFactory.reset();
        }
    }

    @Test
    public void getFileOpener() throws LiquibaseException {
        Liquibase liquibase = new Liquibase("com/example/test.xml", mockResourceAccessor, mockDatabase);
        assertSame(liquibase.getResourceAccessor(), liquibase.getFileOpener());
    }

    @Test
    public void setCurrentDateTimeFunction() throws LiquibaseException {
        Database database = mockDatabase;
        String testFunction = "GetMyTime";

        new Liquibase("com/example/test.xml", mockResourceAccessor, database).setCurrentDateTimeFunction(testFunction);
        verify(database).setCurrentDateTimeFunction(testFunction);
    }

    @Test
    public void update_passedStringContext() throws LiquibaseException {
        LiquibaseDelegate liquibase = new LiquibaseDelegate() {
            @Override
            public void update(Contexts contexts) throws LiquibaseException {
                objectToVerify = contexts;
            }
        };

        liquibase.update("test");
        assertEquals("test", liquibase.objectToVerify.toString());
        liquibase.reset();

        liquibase.update("");
        assertEquals("", liquibase.objectToVerify.toString());
        liquibase.reset();

        liquibase.update((String) null);
        assertEquals("", liquibase.objectToVerify.toString());
        liquibase.reset();

        liquibase.update("test1, test2");
        assertEquals("test1,test2", liquibase.objectToVerify.toString());
        liquibase.reset();
    }

//    @Test
//    public void update() throws LiquibaseException {
//        Contexts contexts = new Contexts("a,b");
//
//        Liquibase liquibase = new Liquibase("com/example/test.xml", mockResourceAccessor, mockDatabase) {
//            @Override
//            protected ChangeLogIterator getStandardChangelogIterator(Contexts contexts, DatabaseChangeLog changeLog) throws DatabaseException {
//                return mockChangeLogIterator;
//            }
//        };
//
//        liquibase.update(contexts);
//
//        verify(mockLockService).waitForLock();
////        verify(mockDatabase).checkDatabaseChangeLogTable(true, mockChangeLog, contexts);
////        verify(mockDatabase).checkDatabaseChangeLogLockTable();
//        verify(mockChangeLog).validate(mockDatabase, contexts);
//        verify(mockChangeLogParser).parse("com/example/test.xml", liquibase.getChangeLogParameters(), mockResourceAccessor);
//        verify(mockChangeLogIterator).run(any(UpdateVisitor.class), eq(mockDatabase));
//        verify(mockLockService).releaseLock();
//        verify(mockDatabase).setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY); //quoting strategy needs to be set back in case it changed during the update
//
//
//        assertEquals("Passed contexts were not set on the changelog parameters object", "a,b", liquibase.getChangeLogParameters().getContexts().toString());
//    }

//    @Test
//    public void update_nullContexts() throws LiquibaseException {
//        Liquibase liquibase = new Liquibase("com/example/test.xml", mockResourceAccessor, mockDatabase) {
//            @Override
//            protected ChangeLogIterator getStandardChangelogIterator(Contexts contexts, DatabaseChangeLog changeLog) throws DatabaseException {
//                return mockChangeLogIterator;
//            }
//        };
//
//        liquibase.update((Contexts) null);
//
//        verify(mockLockService).waitForLock();
////        verify(mockDatabase).checkDatabaseChangeLogTable(true, mockChangeLog, (Contexts) null);
////        verify(mockDatabase).checkDatabaseChangeLogLockTable();
//        verify(mockChangeLog).validate(mockDatabase, (Contexts) null);
//        verify(mockChangeLogParser).parse("com/example/test.xml", liquibase.getChangeLogParameters(), mockResourceAccessor);
//        verify(mockChangeLogIterator).run(any(UpdateVisitor.class), eq(mockDatabase));
//        verify(mockLockService).releaseLock();
//        verify(mockDatabase).setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY); //quoting strategy needs to be set back in case it changed during the update
//
//
//        assertNull(liquibase.getChangeLogParameters().getContexts());
//    }

    @Test(expected = LockException.class)
    public void update_exceptionGettingLock() throws LiquibaseException {

        doThrow(LockException.class).when(mockLockService).waitForLock();

        Liquibase liquibase = new Liquibase("com/example/test.xml", mockResourceAccessor, mockDatabase);
        try {
            liquibase.update((Contexts) null);
        } finally {
            verify(mockLockService).waitForLock();
            //should not call anything else, even releaseLock()
        }
    }

    @Test(expected = ChangeLogParseException.class)
    public void update_exceptionDoingUpdate() throws LiquibaseException {
        Contexts contexts = new Contexts("a,b");

        Liquibase liquibase = new Liquibase("com/example/test.xml", mockResourceAccessor, mockDatabase);

        doThrow(ChangeLogParseException.class).when(mockChangeLogParser).parse("com/example/test.xml", liquibase.getChangeLogParameters(), mockResourceAccessor);

        try {
            liquibase.update(contexts);
        } finally {
            verify(mockLockService).waitForLock();
            verify(mockLockService).releaseLock(); //should still call
            verify(mockDatabase).setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY); //should still call
            verify(mockChangeLogParser).parse("com/example/test.xml", liquibase.getChangeLogParameters(), mockResourceAccessor);
        }

    }

//    @Test
//    public void update_exceptionReleasingLock() throws LiquibaseException {
//        doThrow(LockException.class).when(mockLockService).releaseLock();
//
//        update(); //works like normal, just logs error
//        verify(mockLogger).severe(eq("Could not release lock"), any(Exception.class));
//    }

    @Test
    public void getStandardChangelogIterator() throws LiquibaseException {
        ChangeLogIterator iterator = new Liquibase("com/example/changelog.xml", mockResourceAccessor, mockDatabase).getStandardChangelogIterator(new Contexts("a", "b"), new LabelExpression("x", "y"), mockChangeLog);
        assertListsEqual(new Class[] {ShouldRunChangeSetFilter.class,
                ContextChangeSetFilter.class,
                LabelChangeSetFilter.class,
                DbmsChangeSetFilter.class,
                IgnoreChangeSetFilter.class},
                iterator.getChangeSetFilters(), new Assert.AssertFunction() {
            @Override
            public void check(String message, Object expected, Object actual) {
                assertEquals(message, expected, actual.getClass());
            }
        });
    }


//todo: reintroduce    @Test
//    public void isSaveToRunMigration() throws Exception {
//        TestLiquibase liquibase = testLiquibase;
//
//        // curiously setting the database of mock liquibase
//        Database database = testLiquibase.getDatabase();
//
//        liquibase.setUrl("jdbc:oracle:thin:@localhost:1521:latest");
//        assertTrue(liquibase.isSafeToRunUpdate());
//
//        liquibase.setUrl("jdbc:oracle:thin:@liquibase:1521:latest");
//        assertFalse(liquibase.isSafeToRunUpdate());
//
//        ExecutorService.getInstance().setWriteExecutor(database, new LoggingExecutor(new PrintWriter(System.out), database));
//        assertTrue("Safe to run if outputing sql, even if non-localhost URL", liquibase.isSafeToRunUpdate());
//
//    }

/*    
    @Test
    public void testBlosDocumentation() throws Exception {
    	testLiquibase.generateDocumentation(".");
    }
*/    

//    @Test
//    public void getImplementedDatabases() throws Exception {
//        List<Database> databases = DatabaseFactory.getInstance().getImplementedDatabases();
//        assertTrue(databases.size() > 15);
//
//        boolean foundOracle = false;
//        boolean foundPostgres = false;
//        boolean foundMSSQL = false;
//
//        for (Database db : databases) {
//            if (db instanceof OracleDatabase) {
//                foundOracle = true;
//            } else if (db instanceof PostgresDatabase) {
//                foundPostgres = true;
//            } else if (db instanceof MSSQLDatabase) {
//                foundMSSQL = true;
//            }
//        }
//
//        assertTrue("Oracle not in Implemented Databases", foundOracle);
//        assertTrue("MSSQL not in Implemented Databases", foundMSSQL);
//        assertTrue("Postgres not in Implemented Databases", foundPostgres);
//    }

//    private class TestLiquibase extends Liquibase {
//        private String url;
//        // instead use super.database
//        //private Database database;
//        private InputStream inputStream;
//
//        public TestLiquibase() throws LiquibaseException {
//            super("liquibase/test.xml", new ClassLoaderResourceAccessor(), ((Database) null));
//            inputStream = createMock(InputStream.class);
//            replay(inputStream);
//        }
//
//        @Override
//        public Database getDatabase() {
//            if (database == null) {
//                database = new OracleDatabase() {
//
//                };
//            }
//            return database;
//        }
//
//        public void setDatabase(Database database) {
//            this.database = database;
//        }
//
//
//        @SuppressWarnings("unused")
//		public Database[] getImplementedDatabases() {
//            Database mockDatabase = createMock(Database.class);
//            try {
//
//                expect(mockDatabase.isCorrectDatabaseImplementation(null)).andReturn(true).atLeastOnce();
//                mockDatabase.setConnection((DatabaseConnection) null);
//                expectLastCall();
//                expect(mockDatabase.getConnection()).andReturn(connectionForConstructor);
//                replay(mockDatabase);
//
//                return new Database[]{
//                        mockDatabase,
//                };
//            } catch (DatabaseException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        public void setUrl(String url) {
//            this.url = url;
//        }
//
//        @Override
//        public ResourceAccessor getFileOpener() {
//            return new ResourceAccessor() {
//                public InputStream getResourceAsStream(String file) {
//                    return inputStream;
//                }
//
//                public Enumeration<URL> getResources(String packageName) {
//                    return null;
//                }
//
//                public ClassLoader toClassLoader() {
//                    return null;
//                }
//            };
//        }
//    }

    /**
     * Convenience helper class for testing Liquibase methods that simply delegate to another.
     * To use, create a subclass that overrides the method delegated to with an implementation that stores whatever params are being passed.
     * After calling the delegating method in your test, assert against the objectToVerify
     */
    private static class LiquibaseDelegate extends Liquibase {

        /**
         * If using a single parameter, store in here
         */
        protected Object objectToVerify;

        /**
         * If using multiple parameters, store them here
         */
        protected Map<String, Object> objectsToVerify = new HashMap<String, Object>();

        private LiquibaseDelegate() throws LiquibaseException {
            super("com/example/test.xml", new MockResourceAccessor(), mock(Database.class));
        }

        /**
         * Resets the object(s)ToVerify so this delegate can be reused in a test.
         */
        public void reset() {
            objectToVerify = null;
            objectsToVerify.clear();
        }
    }
}
