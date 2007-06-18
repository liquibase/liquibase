package liquibase.migrator;

import junit.framework.TestCase;
import liquibase.database.Database;
import liquibase.database.MSSQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.database.PostgresDatabase;
import liquibase.migrator.exception.JDBCException;
import static org.easymock.classextension.EasyMock.*;

import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class MigratorTest extends TestCase {
    private TestMigrator testMigrator;
    private Connection connectionForConstructor;

    public void setUp() throws Exception {
        super.setUp();
        if (connectionForConstructor != null) {
            reset(connectionForConstructor);
        }
        connectionForConstructor = createMock(Connection.class);
        connectionForConstructor.setAutoCommit(false);
        expectLastCall().atLeastOnce();

        DatabaseMetaData metaData = createMock(DatabaseMetaData.class);
        expect(metaData.getDatabaseProductName()).andReturn("Oracle");
        replay(metaData);

        expect(connectionForConstructor.getMetaData()).andReturn(metaData);
        replay(connectionForConstructor);

        testMigrator = new TestMigrator();
    }

    public void testIsSaveToRunMigration() throws Exception {
        TestMigrator migrator = testMigrator;

        migrator.setUrl("jdbc:oracle:thin:@localhost:1521:latest");
        assertTrue(migrator.isSafeToRunMigration());

        migrator.setUrl("jdbc:oracle:thin:@liquibase:1521:latest");
        assertFalse(migrator.isSafeToRunMigration());

        migrator.setMode(Migrator.Mode.OUTPUT_SQL_MODE);
        assertTrue("Safe to run if outputing sql, even if non-localhost URL", migrator.isSafeToRunMigration());

    }

    public void testGetImplementedDatabases() throws Exception {
        Migrator migrator = new Migrator(null, new ClassLoaderFileOpener());
        List<Database> databases = Arrays.asList(migrator.getImplementedDatabases());
        assertEquals(4, databases.size());

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

//    public void testMigrate() throws Exception {
//        Digester digester = createMock(Digester.class);
//        AbstractDatabase database = createMock(AbstractDatabase.class);
//        expect(database.getSchemaName()).andReturn("MockSchema").anyTimes();
//        expect(database.getConnectionUsername()).andReturn("MockUsername").anyTimes();
//
//        final boolean[] migrationRulesSetup = new boolean[1];
//        TestMigrator migrator = new TestMigrator() {
//            public void setUpMigrationRules() {
//                migrationRulesSetup[0] = true;
//            }
//        };
//        migrator.setDigester(digester);
//        migrator.setDatabase(database);
//
//        digester.push(migrator);
//        expectLastCall();
//        digester.push(null);
//        expectLastCall();
//        digester.push(migrator.getChangeLogFile());
//        expectLastCall();
//        digester.push(Migrator.EXECUTE_MODE);
//        expectLastCall();
//        digester.push(database);
//        expectLastCall();
//
//        InputStream fileStream = migrator.getFileOpener().getResourceAsStream(null);
//        reset(fileStream);
//
//        expect(digester.parse(isA(BufferedReader.class))).andStubReturn(null);
//        expect(fileStream.read((byte[]) notNull(), anyInt(), anyInt()));
////        expectLastCall().atLeastOnce();
//        fileStream.close();
//        expectLastCall();
//
//        replay(digester);
//        replay(database);
//        replay(fileStream);
//
//        migrator.migrate();
//
//        verify(database);
//        verify(digester);
//        assertTrue("Did not run setupMigrationRules", migrationRulesSetup[0]);
//
//        //-----------------TEST DROP FIRST
//        migrationRulesSetup[0] = false;
//        reset(digester);
//        reset(database);
//        reset(fileStream);
//
//        digester.push(migrator);
//        expectLastCall();
//        digester.push(migrator.getChangeLogFile());
//        expectLastCall();
//        digester.push(null);
//        expectLastCall();
//        digester.push(Migrator.EXECUTE_MODE);
//        expectLastCall();
//        digester.push(database);
//        expectLastCall();
//
//        expect(digester.parse(isA(BufferedReader.class))).andStubReturn(null);
//
//        fileStream.close();
//        expectLastCall();
//
//        expect(database.getSchemaName()).andReturn("testSchema").atLeastOnce();
//        database.dropDatabaseObjects();
//        expectLastCall();
//
//        replay(digester);
//        replay(database);
//        replay(fileStream);
//
//        migrator.setShouldDropDatabaseObjectsFirst(true);
//        migrator.migrate();
//
//        verify(database);
//        verify(digester);
//        assertTrue("Did not run setupMigrationRules", migrationRulesSetup[0]);
//
//
//        //-----------------TEST SAVE MODE FIRST
//        Writer writer = createMock(Writer.class);
//        migrator.setMode(Migrator.OUTPUT_SQL_MODE);
//        migrator.setOutputSQLWriter(writer);
//
//        migrationRulesSetup[0] = false;
//        reset(digester);
//        reset(database);
//        reset(fileStream);
//
//        digester.push(migrator);
//        expectLastCall();
//        digester.push(migrator.getChangeLogFile());
//        expectLastCall();
//        digester.push(Migrator.OUTPUT_SQL_MODE);
//        expectLastCall();
//        digester.push(writer);
//        expectLastCall();
//        digester.push(database);
//        expectLastCall();
//
//        expect(digester.parse(isA(BufferedReader.class))).andStubReturn(null);
//
//        fileStream.close();
//        expectLastCall();
//
//        expect(database.getSchemaName()).andReturn("testSchema").atLeastOnce();
//        expect(database.getConnectionUsername()).andReturn("testUser").atLeastOnce();
//        expect(database.getConnectionURL()).andReturn("jdbc:url:here").atLeastOnce();
//        database.dropDatabaseObjects();
//        expectLastCall();
//
//        replay(digester);
//        replay(database);
//        replay(fileStream);
//
//        writer.write("--------------------------------------------------------------------------------------\n");
//        expectLastCall();
//        writer.write("-- Migration file: "+migrator.getChangeLogFile()+"\n");
//        expectLastCall();
//        writer.write("-- Run at: "+ DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date())+"\n");
//        expectLastCall();
//        writer.write("-- Against: "+database.getConnectionUsername()+"@"+database.getConnectionURL()+"\n");
//        expectLastCall();
//        writer.write("--------------------------------------------------------------------------------------\n\n\n");
//        expectLastCall();
//        replay(writer);
//
//        migrator.setShouldDropDatabaseObjectsFirst(true);
//        migrator.migrate();
//
//        verify(database);
//        verify(digester);
//        verify(writer);
//        assertTrue("Did not run setupMigrationRules", migrationRulesSetup[0]);
//    }

    private class TestMigrator extends Migrator {
        private String url;
        private Database database;
        private InputStream inputStream;

        public TestMigrator() {
            super("liquibase/test.xml", new ClassLoaderFileOpener());
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
                mockDatabase.setConnection(null);
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
            };
        }
    }
}