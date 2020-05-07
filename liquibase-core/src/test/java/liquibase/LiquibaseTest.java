package liquibase;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import liquibase.changelog.*;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.database.core.MockDatabase;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.sdk.resource.MockResourceAccessor;
import liquibase.snapshot.SnapshotGeneratorFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class LiquibaseTest {
    private MockResourceAccessor mockResourceAccessor;
    private Database mockDatabase;
    private LockServiceFactory mockLockServiceFactory;
    private LockService mockLockService;

    private ChangeLogParserFactory mockChangeLogParserFactory;
    private ChangeLogParser mockChangeLogParser;
    private DatabaseChangeLog mockChangeLog;
    private ChangeLogIterator mockChangeLogIterator;

    @Before
    public void before() throws Exception {

        mockResourceAccessor = new MockResourceAccessor();
//        mockDatabase = mock(Database.class);
//        mockLockService = mock(LockService.class);
//        mockLockServiceFactory = mock(LockServiceFactory.class);
//        mockChangeLogParserFactory = mock(ChangeLogParserFactory.class);
//        mockChangeLogParser = mock(ChangeLogParser.class);
//        mockChangeLog = mock(DatabaseChangeLog.class);
//        mockChangeLogIterator = mock(ChangeLogIterator.class);
//
//        mockLogger = mock(Logger.class);

//        LockServiceFactory.setInstance(mockLockServiceFactory);
//        when(mockLockServiceFactory.getLockService(any(Database.class))).thenReturn(mockLockService);

        ChangeLogParserFactory.setInstance(mockChangeLogParserFactory);
//        when(mockChangeLogParserFactory.getParser(anyString(), Mockito.isA(ResourceAccessor.class))).thenReturn(mockChangeLogParser);
//        when(mockChangeLogParser.parse(anyString(), any(ChangeLogParameters.class), Mockito.isA(ResourceAccessor.class))).thenReturn(mockChangeLog);

//        LogService.setLoggerFactory(new LoggerFactory() {
//            @Override
//            public Logger getLog(Class clazz) {
//                return mockLogger;
//            }
//
//            @Override
//            public LoggerContext pushContext(String key, Object object) {
//                return new NoOpLoggerContext();
//            }
//
//            @Override
//            public void close() {
//
//            }
//        });
    }

    @After
    public void after() {
//        verifyNoMoreInteractions(mockLockService, mockChangeLogParser, mockChangeLog, mockChangeLogIterator); //for no other interactions of normal use objects. Not automatically checking mockDatabase and the *Factory mocks
//        Mockito.reset(mockDatabase, mockLockServiceFactory, mockLockService, mockChangeLogParserFactory, mockChangeLogParser, mockChangeLog, mockChangeLogIterator);
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

//    @Test
//    public void testConstructorCreateDatabaseInstanceFromConnection() throws LiquibaseException {
//        DatabaseConnection databaseConnection = mock(DatabaseConnection.class);
//        Database database = mockDatabase;
//
//        try {
//            DatabaseFactory.setInstance(mock(DatabaseFactory.class));
//            when(DatabaseFactory.getInstance().findCorrectDatabaseImplementation(databaseConnection)).thenReturn(database);
//
//            Liquibase liquibase = new Liquibase("com/example/test.xml", mockResourceAccessor, databaseConnection);
//            assertSame("Liquibase constructor passing connection did not find the correct database implementation",
//                database, liquibase.getDatabase());
//
//        } finally {
//            DatabaseFactory.reset();
//        }
//    }

    @Test
    public void testGetResourceAccessor() throws LiquibaseException {
        Liquibase liquibase = new Liquibase("com/example/test.xml", mockResourceAccessor, mockDatabase);
        assertSame("ressourceAccessor is set as requested",
            liquibase.getResourceAccessor(), liquibase.getResourceAccessor());
    }

//    @Test
//    public void testSetCurrentDateTimeFunction() throws LiquibaseException {
//        Database database = mockDatabase;
//        String testFunction = "GetMyTime";
//
//        new Liquibase("com/example/test.xml", mockResourceAccessor, database)
//            .getDatabase()
//            .setCurrentDateTimeFunction(testFunction);
//        verify(database).setCurrentDateTimeFunction(testFunction);
//    }

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

//    @Test(expected = LockException.class)
//    public void testUpdateExceptionGettingLock() throws LiquibaseException {
//
//        doThrow(LockException.class).when(mockLockService).waitForLock();
//
//        Liquibase liquibase = new Liquibase("com/example/test.xml", mockResourceAccessor, mockDatabase);
//        try {
//            liquibase.update((Contexts) null);
//        } finally {
//            verify(mockLockService).waitForLock();
//            //should not call anything else, even releaseLock()
//        }
//    }

//    @Test(expected = ChangeLogParseException.class)
//    public void testUpdateExceptionDoingUpdate() throws LiquibaseException {
//        Contexts contexts = new Contexts("a,b");
//
//        Liquibase liquibase = new Liquibase("com/example/test.xml", mockResourceAccessor, mockDatabase);
//
//        doThrow(ChangeLogParseException.class).when(mockChangeLogParser).parse("com/example/test.xml", liquibase.getChangeLogParameters(), mockResourceAccessor);
//
//        try {
//            liquibase.update(contexts);
//        } finally {
//            verify(mockLockService).waitForLock();
//            verify(mockLockService).releaseLock(); //should still call
//            verify(mockDatabase).setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY); //should still call
//            verify(mockChangeLogParser).parse("com/example/test.xml", liquibase.getChangeLogParameters(), mockResourceAccessor);
//        }
//
//    }
//
//    @Test
//    /* False positive: We do have an assertion in this test. */
//    @SuppressWarnings("squid:S2699")
//    public void testGetStandardChangelogIterator() throws LiquibaseException {
//        ChangeLogIterator iterator =
//            new Liquibase(
//                "com/example/changelog.xml",
//                mockResourceAccessor,
//                mockDatabase
//            ).getStandardChangelogIterator(
//                new Contexts("a", "b"),
//                new LabelExpression("x", "y"),
//                mockChangeLog
//            );
//        assertListsEqual(new Class[] {ShouldRunChangeSetFilter.class,
//                ContextChangeSetFilter.class,
//                LabelChangeSetFilter.class,
//                DbmsChangeSetFilter.class,
//                IgnoreChangeSetFilter.class},
//                iterator.getChangeSetFilters(), new Assert.AssertFunction() {
//            @Override
//            public void check(String message, Object expected, Object actual) {
//                assertEquals(message, expected, actual.getClass());
//            }
//        });
//    }

    @Test
    public void syncChangeLogForUnmanagedDatabase() throws Exception {
        JdbcConnection dbConnection = getInMemoryH2DatabaseConnection();

        try {
            Liquibase liquibase = createUnmanagedDatabase(dbConnection);
            assertFalse(hasDatabaseChangeLogTable(liquibase));

            liquibase.changeLogSync("");
            assertTrue(hasDatabaseChangeLogTable(liquibase));
            assertTags(liquibase, "1.0", "1.1", "2.0");
        }
        finally {
            dbConnection.close();
        }
    }

    @Test
    public void syncChangeLogToTagForUnmanagedDatabase() throws Exception {
        JdbcConnection dbConnection = getInMemoryH2DatabaseConnection();

        try {
            Liquibase liquibase = createUnmanagedDatabase(dbConnection);
            assertFalse(hasDatabaseChangeLogTable(liquibase));

            liquibase.changeLogSync("1.1", "");
            assertTrue(hasDatabaseChangeLogTable(liquibase));
            assertTags(liquibase, "1.0", "1.1");
        }
        finally {
            dbConnection.close();
        }
    }

    @Test
    public void syncChangeLogForManagedDatabase() throws Exception {
        JdbcConnection dbConnection = getInMemoryH2DatabaseConnection();

        try {
            Liquibase liquibase = createDatabaseAtTag(dbConnection, "1.0");
            assertTrue(hasDatabaseChangeLogTable(liquibase));

            liquibase.changeLogSync("");
            assertTags(liquibase, "1.0", "1.1", "2.0");
        }
        finally {
            dbConnection.close();
        }
    }

    @Test
    public void syncChangeLogToTagForManagedDatabase() throws Exception {
        JdbcConnection dbConnection = getInMemoryH2DatabaseConnection();

        try {
            Liquibase liquibase = createDatabaseAtTag(dbConnection, "1.0");
            assertTrue(hasDatabaseChangeLogTable(liquibase));

            liquibase.changeLogSync("1.1", "");
            assertTags(liquibase, "1.0", "1.1");
        }
        finally {
            dbConnection.close();
        }
    }

    @Test
    public void syncChangeLogSqlForUnmanagedDatabase() throws Exception {
        JdbcConnection dbConnection = getInMemoryH2DatabaseConnection();
        StringWriter writer = new StringWriter();

        try {
            Liquibase liquibase = createUnmanagedDatabase(dbConnection);
            assertFalse(hasDatabaseChangeLogTable(liquibase));

            liquibase.changeLogSync("", writer);
            assertFalse(hasDatabaseChangeLogTable(liquibase));
            assertSqlOutputAppliesTags(writer.toString(), "1.0", "1.1", "2.0");
        }
        finally {
            dbConnection.close();
        }
    }

    @Test
    public void syncChangeLogToTagSqlForUnmanagedDatabase() throws Exception {
        JdbcConnection dbConnection = getInMemoryH2DatabaseConnection();
        StringWriter writer = new StringWriter();

        try {
            Liquibase liquibase = createUnmanagedDatabase(dbConnection);
            assertFalse(hasDatabaseChangeLogTable(liquibase));

            liquibase.changeLogSync("1.1", "", writer);
            assertFalse(hasDatabaseChangeLogTable(liquibase));
            assertSqlOutputAppliesTags(writer.toString(), "1.0", "1.1");
        }
        finally {
            dbConnection.close();
        }
    }

    @Test
    public void syncChangeLogSqlForManagedDatabase() throws Exception {
        JdbcConnection dbConnection = getInMemoryH2DatabaseConnection();
        StringWriter writer = new StringWriter();

        try {
            Liquibase liquibase = createDatabaseAtTag(dbConnection, "1.0");
            assertTrue(hasDatabaseChangeLogTable(liquibase));

            liquibase.changeLogSync("", writer);
            assertSqlOutputAppliesTags(writer.toString(), "1.1", "2.0");
        }
        finally {
            dbConnection.close();
        }
    }

    @Test
    public void syncChangeLogToTagSqlForManagedDatabase() throws Exception {
        JdbcConnection dbConnection = getInMemoryH2DatabaseConnection();
        StringWriter writer = new StringWriter();

        try {
            Liquibase liquibase = createDatabaseAtTag(dbConnection, "1.0");
            assertTrue(hasDatabaseChangeLogTable(liquibase));

            liquibase.changeLogSync("1.1", "", writer);
            assertSqlOutputAppliesTags(writer.toString(), "1.1");
        }
        finally {
            dbConnection.close();
        }
    }

    private JdbcConnection getInMemoryH2DatabaseConnection() throws SQLException {
        String urlFormat = "jdbc:h2:mem:%s";
        return new JdbcConnection(DriverManager.getConnection(format(urlFormat, UUID.randomUUID().toString())));
    }

    private Liquibase createUnmanagedDatabase(JdbcConnection connection) throws SQLException, LiquibaseException {
        String createTableSql = "CREATE TABLE PUBLIC.TABLE_A (ID INTEGER);";

        try(PreparedStatement stmt = connection.getUnderlyingConnection().prepareStatement(createTableSql)) {
            stmt.execute();
        }

        return new Liquibase("liquibase/tagged-changelog.xml", new ClassLoaderResourceAccessor(), connection);
    }

    private Liquibase createDatabaseAtTag(JdbcConnection connection, String tag) throws LiquibaseException {
        Liquibase liquibase = new Liquibase("liquibase/tagged-changelog.xml", new ClassLoaderResourceAccessor(),
            connection);
        liquibase.update(tag, "");
        return liquibase;
    }

    private boolean hasDatabaseChangeLogTable(Liquibase liquibase) throws DatabaseException {
        return SnapshotGeneratorFactory.getInstance().hasDatabaseChangeLogTable(liquibase.database);
    }

    private void assertTags(Liquibase liquibase, String... expectedTags) throws DatabaseException {
        List<String> actualTags = liquibase.database.getRanChangeSetList().stream()
            .map(RanChangeSet::getTag)
            .filter(Objects::nonNull)
            .collect(toList());

        assertEquals(Arrays.asList(expectedTags), actualTags);
    }

    private void assertSqlOutputAppliesTags(String output, String... expectedTags) throws IOException {
        String insertTagH2SqlTemplate =
            "INSERT INTO PUBLIC\\.DATABASECHANGELOG \\(.*, DESCRIPTION,.*, TAG\\) VALUES \\(.*, 'tagDatabase',.*, '%s'\\);";

        List<Pattern> patterns = Stream.of(expectedTags)
            .map(tag -> String.format(insertTagH2SqlTemplate, tag))
            .map(Pattern::compile)
            .collect(toList());

        try(BufferedReader reader = new BufferedReader(new StringReader(output))) {
            String line;
            int index = 0;

            while ((line = reader.readLine()) != null && index < patterns.size()) {
                Matcher matcher = patterns.get(index).matcher(line);
                if (matcher.matches()) {
                    index++;
                }
            }
            assertTrue(index > 0 && index == patterns.size());
        }
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
            super("com/example/test.xml", new MockResourceAccessor(), new MockDatabase());
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
