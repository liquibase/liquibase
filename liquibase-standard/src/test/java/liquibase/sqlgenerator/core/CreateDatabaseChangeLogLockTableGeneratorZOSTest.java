package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.DB2Database;
import liquibase.database.core.Db2zConfiguration;
import liquibase.database.core.Db2zDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.sqlgenerator.MockSqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class CreateDatabaseChangeLogLockTableGeneratorZOSTest extends AbstractSqlGeneratorTest<CreateDatabaseChangeLogLockTableStatement> {

    public CreateDatabaseChangeLogLockTableGeneratorZOSTest() throws Exception {
        super(new CreateDatabaseChangeLogLockTableGeneratorZOS());
    }

    @Override
    protected CreateDatabaseChangeLogLockTableStatement createSampleSqlStatement() {
        return new CreateDatabaseChangeLogLockTableStatement();
    }
    
    @Override
    protected boolean shouldBeImplementation(Database database) {
        return database instanceof Db2zDatabase;
    }

    /**
     * Parameterized test class that tests various combinations of DB2 z/OS configuration
     * settings for the DATABASECHANGELOGLOCK table
     */
    @RunWith(Parameterized.class)
    public static class ZOSParameterizedTest {
        
        @Parameters(name = "{0}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {"DefaultSettings", "", "", "", "SCHEMA", true, 
                     "CREATE TABLE \"SCHEMA\".DATABASECHANGELOGLOCK (ID INTEGER NOT NULL, LOCKED SMALLINT NOT NULL, LOCKGRANTED TIMESTAMP, LOCKEDBY VARCHAR(255), PRIMARY KEY (ID))",
                     "CREATE UNIQUE INDEX DATABASECHANGELOGLOCK_PK ON SCHEMA.DATABASECHANGELOGLOCK (ID)"},
                    {"DatabaseOnly", "LBDB", "", "", "SCHEMA", true, 
                     "CREATE TABLE \"SCHEMA\".DATABASECHANGELOGLOCK (ID INTEGER NOT NULL, LOCKED SMALLINT NOT NULL, LOCKGRANTED TIMESTAMP, LOCKEDBY VARCHAR(255), PRIMARY KEY (ID)) IN LBDB",
                     "CREATE UNIQUE INDEX DATABASECHANGELOGLOCK_PK ON SCHEMA.DATABASECHANGELOGLOCK (ID)"},
                    {"TablespaceOnly", "", "LBTS", "", "SCHEMA", true, 
                     "CREATE TABLE \"SCHEMA\".DATABASECHANGELOGLOCK (ID INTEGER NOT NULL, LOCKED SMALLINT NOT NULL, LOCKGRANTED TIMESTAMP, LOCKEDBY VARCHAR(255), PRIMARY KEY (ID)) IN LBTS",
                     "CREATE UNIQUE INDEX DATABASECHANGELOGLOCK_PK ON SCHEMA.DATABASECHANGELOGLOCK (ID)"},
                    {"DatabaseAndTablespace", "LBDB", "LBTS", "", "SCHEMA", true, 
                     "CREATE TABLE \"SCHEMA\".DATABASECHANGELOGLOCK (ID INTEGER NOT NULL, LOCKED SMALLINT NOT NULL, LOCKGRANTED TIMESTAMP, LOCKEDBY VARCHAR(255), PRIMARY KEY (ID)) IN LBDB.LBTS",
                     "CREATE UNIQUE INDEX DATABASECHANGELOGLOCK_PK ON SCHEMA.DATABASECHANGELOGLOCK (ID)"},
                    {"CustomIndexName", "", "", "CUSTOM_INDEX", "SCHEMA", true, 
                     "CREATE TABLE \"SCHEMA\".DATABASECHANGELOGLOCK (ID INTEGER NOT NULL, LOCKED SMALLINT NOT NULL, LOCKGRANTED TIMESTAMP, LOCKEDBY VARCHAR(255), PRIMARY KEY (ID))",
                     "CREATE UNIQUE INDEX CUSTOM_INDEX ON SCHEMA.DATABASECHANGELOGLOCK (ID)"},
                    {"NullSchema", "", "", "", null, false, 
                     "CREATE TABLE DATABASECHANGELOGLOCK (ID INTEGER NOT NULL, LOCKED SMALLINT NOT NULL, LOCKGRANTED TIMESTAMP, LOCKEDBY VARCHAR(255), PRIMARY KEY (ID))",
                     "CREATE UNIQUE INDEX DATABASECHANGELOGLOCK_PK ON DATABASECHANGELOGLOCK (ID)"}
            });
        }
        
        private final String testName;
        private final String databaseName;
        private final String tablespace;
        private final String indexName;
        private final String schema;
        private final boolean hasSchema;
        private final String expectedTableSql;
        private final String expectedIndexSql;
        
        public ZOSParameterizedTest(String testName, String databaseName, String tablespace, 
                                   String indexName, String schema, boolean hasSchema,
                                   String expectedTableSql, String expectedIndexSql) {
            this.testName = testName;
            this.databaseName = databaseName;
            this.tablespace = tablespace;
            this.indexName = indexName;
            this.schema = schema;
            this.hasSchema = hasSchema;
            this.expectedTableSql = expectedTableSql;
            this.expectedIndexSql = expectedIndexSql;
        }
        
        /**
         * Creates a subclass of Db2zDatabase with the test's configuration values
         */
        private Db2zDatabase createCustomDatabase() {
            return new Db2zDatabase() {
                // Override methods to return our test parameters
                @Override
                public String getLiquibaseSchemaName() {
                    return schema;
                }
                
                // Make non-production DB2 instance for testing
                @Override
                public boolean isCorrectDatabaseImplementation(liquibase.database.DatabaseConnection conn) {
                    return true;
                }
                
                // For testing only - otherwise we'd need a real connection
                @Override 
                public boolean requiresUsername() {
                    return false;
                }
                
                @Override
                public boolean requiresPassword() {
                    return false;
                }
            };
        }
        
        /**
         * Creates a custom generator with test configuration settings
         */
        private CreateDatabaseChangeLogLockTableGeneratorZOS createCustomGenerator() {
            return new CreateDatabaseChangeLogLockTableGeneratorZOS() {
                @Override
                protected String getDatabaseChangeLogLockDatabase() {
                    return databaseName;
                }
                
                @Override
                protected String getDatabaseChangeLogLockTablespace() {
                    return tablespace;
                }
                
                @Override
                protected String getDatabaseChangeLogLockIndex() {
                    return indexName;
                }
            };
        }
        
        @Test
        public void testGenerateZOSSql() {
            CreateDatabaseChangeLogLockTableGeneratorZOS generator = createCustomGenerator();
            CreateDatabaseChangeLogLockTableStatement statement = new CreateDatabaseChangeLogLockTableStatement();
            Db2zDatabase database = createCustomDatabase();
            
            SqlGeneratorChain chain = new MockSqlGeneratorChain();
            
            // Generate SQL
            Sql[] sql = generator.generateSql(statement, database, chain);

            assertEquals("Should generate 2 SQL statements", 2, sql.length);
            
            assertEquals("Generated CREATE TABLE SQL should exactly match expected SQL", expectedTableSql, sql[0].toSql());
            assertEquals("Generated CREATE INDEX SQL should exactly match expected SQL", expectedIndexSql, sql[1].toSql());
            
            String createTableSql = sql[0].toSql().toLowerCase();
            String createIndexSql = sql[1].toSql().toLowerCase();
            
            assertTrue("Should contain CREATE TABLE", createTableSql.contains("create table"));
            assertTrue("Should contain ID column", createTableSql.contains("id integer") || createTableSql.contains("id int"));
            assertTrue("Should contain LOCKED column", createTableSql.contains("locked smallint"));
            assertTrue("Should contain LOCKGRANTED column", createTableSql.contains("lockgranted timestamp"));
            assertTrue("Should contain LOCKEDBY column", createTableSql.contains("lockedby varchar"));
            assertTrue("Should have PRIMARY KEY", createTableSql.contains("primary key"));
            
            if (hasSchema && schema != null) {
                assertTrue("Table name should be properly qualified with schema", createTableSql.contains(schema.toLowerCase()) && createTableSql.contains("databasechangeloglock"));
            }
            
            if (!databaseName.isEmpty() && !tablespace.isEmpty()) {
                assertTrue("SQL should include database and tablespace", 
                        createTableSql.contains("in " + databaseName.toLowerCase() + "." + tablespace.toLowerCase()));
            } else if (!databaseName.isEmpty()) {
                assertTrue("SQL should include only database", 
                        createTableSql.contains("in " + databaseName.toLowerCase()));
            } else if (!tablespace.isEmpty()) {
                assertTrue("SQL should include only tablespace", 
                        createTableSql.contains("in " + tablespace.toLowerCase()));
            }
            
            assertTrue("Should contain CREATE UNIQUE INDEX", createIndexSql.contains("create unique index"));
            
            String expectedIndexNamePattern = indexName.isEmpty() ? "databasechangeloglock_pk" : indexName.toLowerCase();
            assertTrue("Should use correct index name", createIndexSql.contains(expectedIndexNamePattern));
            assertTrue("Index should be on ID column", createIndexSql.contains("(id)"));
        }
    }
}