package liquibase


import liquibase.changelog.ChangeLogIterator
import liquibase.changelog.DatabaseChangeLog
import liquibase.configuration.HubConfiguration
import liquibase.configuration.LiquibaseConfiguration
import liquibase.database.Database
import liquibase.database.core.MockDatabase
import liquibase.exception.LiquibaseException
import liquibase.hub.HubService
import liquibase.hub.HubServiceFactory
import liquibase.hub.core.MockHubService
import liquibase.hub.model.Connection
import liquibase.lockservice.LockService
import liquibase.lockservice.LockServiceFactory
import liquibase.parser.ChangeLogParser
import liquibase.parser.ChangeLogParserFactory
import liquibase.parser.MockChangeLogParser
import liquibase.sdk.resource.MockResourceAccessor
import liquibase.ui.ConsoleUIService
import liquibase.ui.ConsoleUIServiceTest
import spock.lang.Specification

import static org.junit.Assert.*

class LiquibaseTest extends Specification {

    private String setupScopeId
    private MockResourceAccessor mockResourceAccessor
    private Database mockDatabase
    private LockServiceFactory mockLockServiceFactory
    private LockService mockLockService
    private MockHubService mockHubService

    private ChangeLogParserFactory mockChangeLogParserFactory
    private ChangeLogParser mockChangeLogParser
    private DatabaseChangeLog mockChangeLog
    private ChangeLogIterator mockChangeLogIterator

    def setup() {
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
        setupScopeId = Scope.enter([
                ("liquibase.plugin." + HubService.name): MockHubService,
        ])

        mockHubService = (MockHubService) Scope.currentScope.getSingleton(HubServiceFactory).getService()
        mockHubService.reset()
        ChangeLogParserFactory.getInstance().register(new MockChangeLogParser(changeLogs: [
                "com/example/changelog.mock": new DatabaseChangeLog(changeLogId: MockHubService.randomUUID.toString())
        ]))
    }

    def cleanup() {
//        verifyNoMoreInteractions(mockLockService, mockChangeLogParser, mockChangeLog, mockChangeLogIterator); //for no other interactions of normal use objects. Not automatically checking mockDatabase and the *Factory mocks
//        Mockito.reset(mockDatabase, mockLockServiceFactory, mockLockService, mockChangeLogParserFactory, mockChangeLogParser, mockChangeLog, mockChangeLogIterator);
        mockHubService.reset()
        LockServiceFactory.reset()
        ChangeLogParserFactory.reset()
        Scope.exit(setupScopeId)
        ChangeLogParserFactory.reset()
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

    def "update communicates with hub"() {
        given:
        Liquibase liquibase = new Liquibase("com/example/changelog.mock", mockResourceAccessor, mockDatabase)
        LiquibaseConfiguration.instance.getConfiguration(HubConfiguration.class).setLiquibaseHubApiKey("API_KEY")

        when:
        liquibase.update("")

        then:
        mockHubService.sentObjects.toString() ==
            "[setRanChangeSets/Connection jdbc://test ($MockHubService.randomUUID):[test/changelog.xml::1::mock-author, test/changelog.xml::2::mock-author, test/changelog.xml::3::mock-author], startOperation/$MockHubService.randomUUID:[$MockHubService.operationCreateDate]]"

    }

    def "getConnection returns warning message if API key does not exist and the changelog is registered"() {
        given:
        Map<String, Object> scopedObjects = new HashMap<>()
        TestConsoleUIService uiService = new TestConsoleUIService()
        scopedObjects.put(Scope.Attr.ui.name(), uiService)
        def scopeId = Scope.enter(null, scopedObjects)

        when:
        Liquibase liquibase = new Liquibase("com/example/changelog.mock", mockResourceAccessor, mockDatabase)
        LiquibaseConfiguration.instance.getConfiguration(HubConfiguration.class).setLiquibaseHubApiKey(null)
        DatabaseChangeLog changeLog = liquibase.getDatabaseChangeLog()
        def changeLogId = UUID.randomUUID().toString()
        changeLog.setChangeLogId(changeLogId)
        Connection connection = liquibase.getConnection(changeLog)
        List<String> messages = uiService.getMessages()
        String message = messages.get(0)
        Scope.exit(scopeId)

        then:
        connection == null
        message ==
                "WARNING: The changelog ID '" + changeLogId + "' was found, but no API Key exists.\n" +
                "No operations will be reported. Simply add a liquibase.hub.apiKey setting to generate free deployment reports.\n" +
                "Learn more at https://hub.liquibase.com."
    }

    def "getConnection returns warning message if API key exists but the changelog is not registered"() {
        given:
        Map<String, Object> scopedObjects = new HashMap<>()
        TestConsoleUIService uiService = new TestConsoleUIService()
        scopedObjects.put(Scope.Attr.ui.name(), uiService)
        def scopeId = Scope.enter(null, scopedObjects)

        when:
        Liquibase liquibase = new Liquibase("com/example/changelog.mock", mockResourceAccessor, mockDatabase)
        LiquibaseConfiguration.instance.getConfiguration(HubConfiguration.class).setLiquibaseHubApiKey("API_KEY")
        DatabaseChangeLog changeLog = liquibase.getDatabaseChangeLog()
        changeLog.setChangeLogId(null)
        Connection connection = liquibase.getConnection(changeLog)
        List<String> messages = uiService.getMessages()
        String message = messages.get(0)
        Scope.exit(scopeId)

        then:
        connection == null
        message ==
          "WARNING: The API key 'API_KEY' was found, but no changelog ID exists.\n" +
          "No operations will be reported. Register this changelog with Liquibase Hub to generate free deployment reports.\n" +
          "Learn more at https://hub.liquibase.com."
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
