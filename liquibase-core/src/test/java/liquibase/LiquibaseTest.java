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
import liquibase.exception.ChangeLogParseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.LogService;
import liquibase.logging.Logger;
import liquibase.logging.LoggerContext;
import liquibase.logging.LoggerFactory;
import liquibase.logging.core.NoOpLoggerContext;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ResourceAccessor;
import liquibase.sdk.database.MockDatabase;
import liquibase.sdk.resource.MockResourceAccessor;
import liquibase.test.Assert;
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

    @Before
    public void before() throws Exception {

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

        LogService.setLoggerFactory(new LoggerFactory() {
            @Override
            public Logger getLog(Class clazz) {
                return mockLogger;
            }

            @Override
            public LoggerContext pushContext(String key, Object object) {
                return new NoOpLoggerContext();
            }

            @Override
            public void close() {

            }
        });
    }

    @After
    public void after() {
        verifyNoMoreInteractions(mockLockService, mockChangeLogParser, mockChangeLog, mockChangeLogIterator); //for no other interactions of normal use objects. Not automatically checking mockDatabase and the *Factory mocks
        Mockito.reset(mockDatabase, mockLockServiceFactory, mockLockService, mockChangeLogParserFactory, mockChangeLogParser, mockChangeLog, mockChangeLogIterator);
        LockServiceFactory.reset();
        ChangeLogParserFactory.reset();
    }

    @Test
    public void testConstructor() throws Exception {
        MockResourceAccessor resourceAccessor = this.mockResourceAccessor;
        MockDatabase database = new MockDatabase();

        Liquibase liquibase = new Liquibase("com/example/test.xml", resourceAccessor, database);

        assertNotNull("change log object may not be null", liquibase.getLog());

        assertEquals("correct name of the change log file is returned",
        "com/example/test.xml", liquibase.getChangeLogFile());

        assertSame("ressourceAccessor property is set as requested",
            resourceAccessor, liquibase.getResourceAccessor());

        assertNotNull("parameters list for the change log is not null",
            liquibase.getChangeLogParameters());
        assertEquals("Standard database changelog parameters were not set",
            "DATABASECHANGELOGLOCK",
            liquibase.getChangeLogParameters().getValue("database.databaseChangeLogLockTableName", null)
        );

        assertSame("database object for the change log is set as requested",
            database, liquibase.getDatabase());
    }

    @Test
    public void testConstructorChangelogPathsStandardize() throws Exception {
        Liquibase liquibase = new Liquibase("path\\with\\windows\\separators.xml", mockResourceAccessor, new MockDatabase());
        assertEquals("Windows path separators are translated correctly",
            "path/with/windows/separators.xml", liquibase.getChangeLogFile());

        liquibase = new Liquibase("path/with/unix/separators.xml", mockResourceAccessor, new MockDatabase());
        assertEquals("Unix path separators are left intact",
            "path/with/unix/separators.xml", liquibase.getChangeLogFile());

        liquibase = new Liquibase("/absolute/path/remains.xml", mockResourceAccessor, new MockDatabase());
        assertEquals("An absolute path is left intact",
            "/absolute/path/remains.xml", liquibase.getChangeLogFile());
    }

    @Test
    public void testConstructorCreateDatabaseInstanceFromConnection() throws LiquibaseException {
        DatabaseConnection databaseConnection = mock(DatabaseConnection.class);
        Database database = mockDatabase;

        try {
            DatabaseFactory.setInstance(mock(DatabaseFactory.class));
            when(DatabaseFactory.getInstance().findCorrectDatabaseImplementation(databaseConnection)).thenReturn(database);

            Liquibase liquibase = new Liquibase("com/example/test.xml", mockResourceAccessor, databaseConnection);
            assertSame("Liquibase constructor passing connection did not find the correct database implementation",
                database, liquibase.getDatabase());

        } finally {
            DatabaseFactory.reset();
        }
    }

    @Test
    public void testGetResourceAccessor() throws LiquibaseException {
        Liquibase liquibase = new Liquibase("com/example/test.xml", mockResourceAccessor, mockDatabase);
        assertSame("ressourceAccessor is set as requested",
            liquibase.getResourceAccessor(), liquibase.getResourceAccessor());
    }

    @Test
    public void testSetCurrentDateTimeFunction() throws LiquibaseException {
        Database database = mockDatabase;
        String testFunction = "GetMyTime";

        new Liquibase("com/example/test.xml", mockResourceAccessor, database)
            .getDatabase()
            .setCurrentDateTimeFunction(testFunction);
        verify(database).setCurrentDateTimeFunction(testFunction);
    }

    @Test
    public void testUpdatePassedStringContext() throws LiquibaseException {
        LiquibaseDelegate liquibase = new LiquibaseDelegate() {
            @Override
            public void update(Contexts contexts) throws LiquibaseException {
                objectToVerify = contexts;
            }
        };

        liquibase.update("test");
        assertEquals("context is set correctly", "test", liquibase.objectToVerify.toString());
        liquibase.reset();

        liquibase.update("");
        assertEquals("context is set correctly", "", liquibase.objectToVerify.toString());
        liquibase.reset();

        liquibase.update((String) null);
        assertEquals("context is set correctly", "", liquibase.objectToVerify.toString());
        liquibase.reset();

        liquibase.update("test1, test2");
        assertEquals("context is set correctly", "test1,test2", liquibase.objectToVerify.toString());
        liquibase.reset();
    }

    @Test(expected = LockException.class)
    public void testUpdateExceptionGettingLock() throws LiquibaseException {

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
    public void testUpdateExceptionDoingUpdate() throws LiquibaseException {
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

    @Test
    /* False positive: We do have an assertion in this test. */
    @SuppressWarnings("squid:S2699")
    public void testGetStandardChangelogIterator() throws LiquibaseException {
        ChangeLogIterator iterator =
            new Liquibase(
                "com/example/changelog.xml",
                mockResourceAccessor,
                mockDatabase
            ).getStandardChangelogIterator(
                new Contexts("a", "b"),
                new LabelExpression("x", "y"),
                mockChangeLog
            );
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

    /**
     * Convenience helper class for testing Liquibase methods that simply delegate to another.
     * To use, create a subclass that overrides the method delegated to with an implementation that stores whatever params are being passed.
     * After calling the delegating method in your test, assert against the objectToVerify
     */
    private static class LiquibaseDelegate extends Liquibase {

        /**
         * If using multiple parameters, store them here
         */
        protected final Map<String, Object> objectsToVerify = new HashMap<>();
        /**
         * If using a single parameter, store in here
         */
        protected Object objectToVerify;

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
