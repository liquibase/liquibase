package liquibase;

import liquibase.ClassLoaderFileOpener;
import liquibase.FileOpener;
import liquibase.Liquibase;
import liquibase.lock.LockHandler;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.DatabaseTest;
import liquibase.test.JdbcDatabaseTest;
import liquibase.database.*;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.sql.DropTableStatement;
import liquibase.database.template.JdbcOutputTemplate;
import liquibase.database.template.JdbcTemplate;
import liquibase.exception.JDBCException;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.DatabaseMetaData;
import java.util.Enumeration;
import java.util.List;

/**
 * Tests for {@link liquibase.Liquibase}
 */
public class LiquibaseTest {

    private TestLiquibase testLiquibase;
    private DatabaseConnection connectionForConstructor;

    @Before
    public void setUp() throws Exception {
        if (connectionForConstructor != null) {
            reset(connectionForConstructor);
        }
        connectionForConstructor = createMock(DatabaseConnection.class);
        connectionForConstructor.setAutoCommit(false);
        expectLastCall().atLeastOnce();

        DatabaseMetaData metaData = createMock(DatabaseMetaData.class);
        expect(metaData.getDatabaseProductName()).andReturn("Oracle");
        replay(metaData);

        expect(connectionForConstructor.getMetaData()).andReturn(metaData);
        replay(connectionForConstructor);

        testLiquibase = new TestLiquibase();
    }

    @Test
    public void isSaveToRunMigration() throws Exception {
        TestLiquibase liquibase = testLiquibase;

        liquibase.setUrl("jdbc:oracle:thin:@localhost:1521:latest");
        assertTrue(liquibase.isSafeToRunMigration());

        liquibase.setUrl("jdbc:oracle:thin:@liquibase:1521:latest");
        assertFalse(liquibase.isSafeToRunMigration());

        testLiquibase.getDatabase().setJdbcTemplate(new JdbcOutputTemplate(new PrintWriter(System.out), testLiquibase.getDatabase()));
        assertTrue("Safe to run if outputing sql, even if non-localhost URL", liquibase.isSafeToRunMigration());

    }

    @Test
    public void getImplementedDatabases() throws Exception {
        List<Database> databases = DatabaseFactory.getInstance().getImplementedDatabases();
        assertEquals(13, databases.size());

        boolean foundOracle = false;
        boolean foundPostgres = false;
        boolean foundMSSQL = false;

        for (Database db : databases) {
            if (db instanceof OracleDatabase) {
                foundOracle = true;
            } else if (db instanceof PostgresDatabase) {
                foundPostgres = true;
            } else if (db instanceof MSSQLDatabase) {
                foundMSSQL = true;
            }
        }

        assertTrue("Oracle not in Implemented Databases", foundOracle);
        assertTrue("MSSQL not in Implemented Databases", foundMSSQL);
        assertTrue("Postgres not in Implemented Databases", foundPostgres);
    }

    private class TestLiquibase extends Liquibase {
        private String url;
        private Database database;
        private InputStream inputStream;

        public TestLiquibase() {
            super("liquibase/test.xml", new ClassLoaderFileOpener(), ((Database) null));
            inputStream = createMock(InputStream.class);
            replay(inputStream);
        }

        public Database getDatabase() {
            if (database == null) {
                database = new OracleDatabase() {
                    public String getConnectionURL() {
                        return url;
                    }

                    public String getConnectionUsername() {
                        return "testUser";
                    }
                };
            }
            return database;
        }

        public void setDatabase(Database database) {
            this.database = database;
        }


        public Database[] getImplementedDatabases() {
            Database mockDatabase = createMock(Database.class);
            try {

                expect(mockDatabase.isCorrectDatabaseImplementation(null)).andReturn(true).atLeastOnce();
                mockDatabase.setConnection((DatabaseConnection) null);
                expectLastCall();
                expect(mockDatabase.getConnection()).andReturn(connectionForConstructor);
                replay(mockDatabase);

                return new Database[]{
                        mockDatabase,
                };
            } catch (JDBCException e) {
                throw new RuntimeException(e);
            }
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public FileOpener getFileOpener() {
            return new FileOpener() {
                public InputStream getResourceAsStream(String file) {
                    return inputStream;
                }

                public Enumeration<URL> getResources(String packageName) {
                    return null;
                }

                public ClassLoader toClassLoader() {
                    return null;
                }
            };
        }
    }
}
