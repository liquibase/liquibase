package liquibase;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.MockDatabase;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogLevel;
import liquibase.test.MockResourceAccessor;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LiquibaseTest {

//    private TestLiquibase testLiquibase;
//    private DatabaseConnection connectionForConstructor;

    @Before
    public void setUp() throws Exception {
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
    }

    @Test
    public void constructor() throws Exception {
        MockResourceAccessor resourceAccessor = new MockResourceAccessor();
        MockDatabase database = new MockDatabase();

        Liquibase liquibase = new Liquibase("com/example/test.xml", resourceAccessor, database);

        assertNotNull(liquibase.getLog());
        assertEquals("Log level should default to INFO", LogLevel.INFO, liquibase.getLog().getLogLevel());

        assertEquals("com/example/test.xml", liquibase.getChangeLogFile());


        assertSame(resourceAccessor, liquibase.getResourceAccessor());

        assertNotNull(liquibase.getChangeLogParameters());
        assertEquals("Standard database changelog parameters were not set", "DATABASECHANGELOGLOCK", liquibase.getChangeLogParameters().getValue("database.databaseChangeLogLockTableName"));

        assertSame(database, liquibase.getDatabase());
    }

    @Test
    public void constructor_changelogPathsStandardize() throws Exception {
        Liquibase liquibase = new Liquibase("path\\with\\windows\\separators.xml", new MockResourceAccessor(), new MockDatabase());
        assertEquals("path/with/windows/separators.xml", liquibase.getChangeLogFile());

        liquibase = new Liquibase("path/with/unix/separators.xml", new MockResourceAccessor(), new MockDatabase());
        assertEquals("path/with/unix/separators.xml", liquibase.getChangeLogFile());

        liquibase = new Liquibase("/absolute/path/remains.xml", new MockResourceAccessor(), new MockDatabase());
        assertEquals("/absolute/path/remains.xml", liquibase.getChangeLogFile());
    }

    @Test
    public void constructor_createDatabaseInstanceFromConnection() throws LiquibaseException {
        DatabaseConnection databaseConnection = mock(DatabaseConnection.class);
        Database database = mock(Database.class);

        try {
            DatabaseFactory.setInstance(mock(DatabaseFactory.class));
            when(DatabaseFactory.getInstance().findCorrectDatabaseImplementation(databaseConnection)).thenReturn(database);

            Liquibase liquibase = new Liquibase("com/example/test.xml", new MockResourceAccessor(), databaseConnection);
            assertSame("Liquibase constructor passing connection did not find the correct database implementation", database, liquibase.getDatabase());

        } finally {
            DatabaseFactory.reset();
        }
    }

    @Test
    public void getFileOpener() throws LiquibaseException {
        Liquibase liquibase = new Liquibase("com/example/test.xml", new MockResourceAccessor(), mock(Database.class));
        assertSame(liquibase.getResourceAccessor(), liquibase.getFileOpener());
    }

    @Test
    public void setCurrentDateTimeFunction() throws LiquibaseException {
        Database database = mock(Database.class);
        String testFunction = "GetMyTime";

        new Liquibase("com/example/test.xml", new MockResourceAccessor(), database).setCurrentDateTimeFunction(testFunction);
        verify(database).setCurrentDateTimeFunction(testFunction);
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
}
