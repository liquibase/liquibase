package liquibase

import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.changelog.ChangeLogIterator
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.changelog.RanChangeSet
import liquibase.database.Database
import liquibase.database.core.H2Database
import liquibase.database.core.MockDatabase
import liquibase.database.core.PostgresDatabase
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.DatabaseException
import liquibase.exception.LiquibaseException
import liquibase.lockservice.LockService
import liquibase.lockservice.LockServiceFactory
import liquibase.parser.ChangeLogParser
import liquibase.parser.ChangeLogParserFactory
import liquibase.parser.MockChangeLogParser
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.sdk.resource.MockResourceAccessor
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.ui.ConsoleUIService
import spock.lang.Specification

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.regex.Matcher
import java.util.regex.Pattern

import static java.lang.String.format
import static org.junit.Assert.*

class LiquibaseTest extends Specification {

    private String setupScopeId
    private MockResourceAccessor mockResourceAccessor
    private Database mockDatabase
    private LockServiceFactory mockLockServiceFactory
    private LockService mockLockService

    private ChangeLogParserFactory mockChangeLogParserFactory
    private ChangeLogParser mockChangeLogParser
    private DatabaseChangeLog mockChangeLog
    private ChangeLogIterator mockChangeLogIterator

    JdbcConnection h2Connection

    def setup() {
        h2Connection = null;
        mockResourceAccessor = new MockResourceAccessor()
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

        ChangeLogParserFactory.setInstance(mockChangeLogParserFactory)
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

        mockDatabase = new MockDatabase()
        setupScopeId = Scope.enter(null)
        def databaseChangeLog = new DatabaseChangeLog()
        databaseChangeLog.addChangeSet(new ChangeSet(
                "test",
                "test",
                false,
                false,
                "test",
                "",
                "",
                databaseChangeLog));
        ChangeLogParserFactory.getInstance().register(new MockChangeLogParser(changeLogs: [
                "com/example/changelog.mock":
                        databaseChangeLog
        ]))
    }

    def cleanup() {
//        verifyNoMoreInteractions(mockLockService, mockChangeLogParser, mockChangeLog, mockChangeLogIterator); //for no other interactions of normal use objects. Not automatically checking mockDatabase and the *Factory mocks
//        Mockito.reset(mockDatabase, mockLockServiceFactory, mockLockService, mockChangeLogParserFactory, mockChangeLogParser, mockChangeLog, mockChangeLogIterator);
        LockServiceFactory.reset()
        ChangeLogParserFactory.reset()
        Scope.exit(setupScopeId)
        ChangeLogParserFactory.reset()

        if (h2Connection != null) {
            h2Connection.close()
        }
    }


    def testConstructor() throws Exception {
        when:

        MockResourceAccessor resourceAccessor = this.mockResourceAccessor

        Liquibase liquibase = new Liquibase("com/example/test.xml", mockResourceAccessor, mockDatabase)

        then:
        assertNotNull("change log object may not be null", liquibase.getLog())

        assertEquals("correct name of the change log file is returned",
                "com/example/test.xml", liquibase.getChangeLogFile())

        assertSame("ressourceAccessor property is set as requested",
                resourceAccessor, liquibase.getResourceAccessor())

        assertNotNull("parameters list for the change log is not null",
                liquibase.getChangeLogParameters())
        assertEquals("Standard database changelog parameters were not set",
                "DATABASECHANGELOGLOCK",
                liquibase.getChangeLogParameters().getValue("database.databaseChangeLogLockTableName", null)
        )

        assertSame("database object for the change log is set as requested",
                mockDatabase, liquibase.getDatabase())
    }

    def testConstructorChangelogPathsStandardize() throws Exception {
        when:
        Liquibase liquibase = new Liquibase("path\\with\\windows\\separators.xml", mockResourceAccessor, mockDatabase)

        then:
        assertEquals("Windows path separators are translated correctly",
                "path/with/windows/separators.xml", liquibase.getChangeLogFile())

        when:
        liquibase = new Liquibase("path/with/unix/separators.xml", mockResourceAccessor, mockDatabase)
        then:
        assertEquals("Unix path separators are left intact",
                "path/with/unix/separators.xml", liquibase.getChangeLogFile())

        when:
        liquibase = new Liquibase("/absolute/path/remains.xml", mockResourceAccessor, mockDatabase)
        then:
        assertEquals("An absolute path is left intact",
                "/absolute/path/remains.xml", liquibase.getChangeLogFile())
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

    def testGetResourceAccessor() throws LiquibaseException {
        when:
        Liquibase liquibase = new Liquibase("com/example/test.xml", mockResourceAccessor, (Database) mockDatabase)

        then:
        assertSame("ressourceAccessor is set as requested",
                liquibase.getResourceAccessor(), liquibase.getResourceAccessor())
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


    def syncChangeLogForUnmanagedDatabase() throws Exception {
        when:
        h2Connection = getInMemoryH2DatabaseConnection();

        Liquibase liquibase = createUnmanagedDatabase(h2Connection);
        assertFalse(hasDatabaseChangeLogTable(liquibase));

        liquibase.changeLogSync("");

        then:
        assert hasDatabaseChangeLogTable(liquibase);
        assertTags(liquibase, "1.0", "1.1", "2.0");
    }

    def syncChangeLogToTagForUnmanagedDatabase() throws Exception {
        when:
        h2Connection = getInMemoryH2DatabaseConnection();

        Liquibase liquibase = createUnmanagedDatabase(h2Connection);

        then:
        assert !hasDatabaseChangeLogTable(liquibase)

        when:
        liquibase.changeLogSync("1.1", "");

        then:
        assert hasDatabaseChangeLogTable(liquibase);
        assertTags(liquibase, "1.0", "1.1");
    }

    def syncChangeLogForManagedDatabase() throws Exception {
        when:
        h2Connection = getInMemoryH2DatabaseConnection();

        Liquibase liquibase = createDatabaseAtTag(h2Connection, "1.0");

        then:
        assert hasDatabaseChangeLogTable(liquibase)

        when:
        liquibase.changeLogSync("");

        then:
        assertTags(liquibase, "1.0", "1.1", "2.0");
    }

    def syncChangeLogToTagForManagedDatabase() throws Exception {
        when:
        h2Connection = getInMemoryH2DatabaseConnection();

        Liquibase liquibase = createDatabaseAtTag(h2Connection, "1.0");
        then:
        assert hasDatabaseChangeLogTable(liquibase);

        when:
        liquibase.changeLogSync("1.1", "");

        then:
        assertTags(liquibase, "1.0", "1.1");
    }

    def syncChangeLogSqlForUnmanagedDatabase() throws Exception {
        when:
        h2Connection = getInMemoryH2DatabaseConnection();
        StringWriter writer = new StringWriter();

        Liquibase liquibase = createUnmanagedDatabase(h2Connection);

        then:
        assert !hasDatabaseChangeLogTable(liquibase);

        when:
        liquibase.changeLogSync("", writer);

        then:
        assert !hasDatabaseChangeLogTable(liquibase);
        assertSqlOutputAppliesTags(writer.toString(), "1.0", "1.1", "2.0");
    }

    def syncChangeLogToTagSqlForUnmanagedDatabase() throws Exception {
        when:
        h2Connection = getInMemoryH2DatabaseConnection();
        StringWriter writer = new StringWriter();

        Liquibase liquibase = createUnmanagedDatabase(h2Connection);

        then:
        assert !hasDatabaseChangeLogTable(liquibase);

        when:
        liquibase.changeLogSync("1.1", "", writer);

        then:
        !hasDatabaseChangeLogTable(liquibase);
        assertSqlOutputAppliesTags(writer.toString(), "1.0", "1.1");
    }

    def syncChangeLogSqlForManagedDatabase() throws Exception {
        when:
        h2Connection = getInMemoryH2DatabaseConnection();
        StringWriter writer = new StringWriter();

        Liquibase liquibase = createDatabaseAtTag(h2Connection, "1.0");

        then:
        assert hasDatabaseChangeLogTable(liquibase);

        when:
        liquibase.changeLogSync("", writer);

        then:
        assertSqlOutputAppliesTags(writer.toString(), "1.1", "2.0");
    }

    def syncChangeLogToTagSqlForManagedDatabase() throws Exception {
        when:
        h2Connection = getInMemoryH2DatabaseConnection();
        StringWriter writer = new StringWriter();

        Liquibase liquibase = createDatabaseAtTag(h2Connection, "1.0");

        then:
        assertTrue(hasDatabaseChangeLogTable(liquibase));

        when:
        liquibase.changeLogSync("1.1", "", writer);

        then:
        assertSqlOutputAppliesTags(writer.toString(), "1.1");
    }

    def validateContextLabelEntryHasNotBeenAddedPreviously() {
        when:
        h2Connection = getInMemoryH2DatabaseConnection()
        Liquibase liquibase = createDatabaseAtTag(h2Connection, "1.0")
        Contexts context = new Contexts("testContext")
        LabelExpression label = new LabelExpression("testLabel")

        then:
        assertFalse(liquibase.isUpToDateFastCheck(context, label))

    }

    def validateContextLabelEntryHasBeenAddedPreviously() {
        when:
        h2Connection = getInMemoryH2DatabaseConnection()
        Liquibase liquibase = new Liquibase("liquibase/test-changelog-fast-check.xml", new ClassLoaderResourceAccessor(),
                h2Connection)
        Contexts context = new Contexts("testContext")
        LabelExpression label = new LabelExpression("testLabel")
        liquibase.update()

        then:
        assertTrue(liquibase.isUpToDateFastCheck(context, label))

    }

    def "validate checksums from ran changesets have all been reset"() {
        when:
        h2Connection = getInMemoryH2DatabaseConnection()
        Liquibase liquibase = new Liquibase("liquibase/test-changelog-fast-check.xml", new ClassLoaderResourceAccessor(),
                h2Connection)
        liquibase.update()
        liquibase.clearCheckSums()

        then:
        List<RanChangeSet> ranChangeSets = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(liquibase.getDatabase()).getRanChangeSets()
        assert ranChangeSets.get(0).getLastCheckSum() == null
    }


    private JdbcConnection getInMemoryH2DatabaseConnection() throws SQLException {
        String urlFormat = "jdbc:h2:mem:%s";
        return new JdbcConnection(DriverManager.getConnection(format(urlFormat, UUID.randomUUID().toString())));
    }

    private Liquibase createUnmanagedDatabase(JdbcConnection connection) throws SQLException, LiquibaseException {
        String createTableSql = "CREATE TABLE PUBLIC.TABLE_A (ID INTEGER);";

        PreparedStatement stmt = connection.getUnderlyingConnection().prepareStatement(createTableSql)
        try {
            stmt.execute();
        } finally {
            stmt.close()
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
        def actualTags = []
        for (def ranChangeset : liquibase.database.getRanChangeSetList()) {
            if (ranChangeset.getTag() != null) {
                actualTags.add(ranChangeset.getTag())
            }
        }

        assertEquals(Arrays.asList(expectedTags), actualTags);
    }

    private void assertSqlOutputAppliesTags(String output, String... expectedTags) throws IOException {
        String insertTagH2SqlTemplate =
                "INSERT INTO PUBLIC\\.DATABASECHANGELOG \\(.*, DESCRIPTION,.*, TAG\\) VALUES \\(.*, 'tagDatabase',.*, '%s'\\);";

        List<Pattern> patterns = []

        for (def tag : expectedTags) {
            patterns.add(Pattern.compile(String.format(insertTagH2SqlTemplate, tag)))
        }

        BufferedReader reader = new BufferedReader(new StringReader(output))
        try {
            String line;
            int index = 0;

            while ((line = reader.readLine()) != null && index < patterns.size()) {
                Matcher matcher = patterns.get(index).matcher(line);
                if (matcher.matches()) {
                    index++;
                }
            }
            assertTrue(index > 0 && index == patterns.size());
        } finally {
            reader.close()
        }
    }

    public static class TestConsoleUIService extends ConsoleUIService {
        private List<String> messages = new ArrayList<>()

        @Override
        void sendMessage(String message) {
            messages.add(message)
        }

        List<String> getMessages() {
            return messages
        }
    }

    /**
     * Convenience helper class for testing Liquibase methods that simply delegate to another.
     * To use, create a subclass that overrides the method delegated to with an implementation that stores whatever params are being passed.
     * After calling the delegating method in your test, assert against the objectToVerify
     */
    public static class LiquibaseDelegate extends Liquibase {

        /**
         * If using multiple parameters, store them here
         */
        protected final Map<String, Object> objectsToVerify = new HashMap<>()
        /**
         * If using a single parameter, store in here
         */
        protected Object objectToVerify

        public LiquibaseDelegate() throws LiquibaseException {
            super("com/example/changelog.mock", new MockResourceAccessor(), mockDatabase)
        }

        /**
         * Resets the object(s)ToVerify so this delegate can be reused in a test.
         */
        public void reset() {
            objectToVerify = null
            objectsToVerify.clear()
        }
    }
}
