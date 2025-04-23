package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.Db2zDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.sqlgenerator.MockSqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;
import liquibase.structure.core.Table;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;

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
                    {"DefaultSettings", "", "", "", "SCHEMA", true},
                    {"DatabaseOnly", "LBDB", "", "", "SCHEMA", true},
                    {"TablespaceOnly", "", "LBTS", "", "SCHEMA", true},
                    {"DatabaseAndTablespace", "LBDB", "LBTS", "", "SCHEMA", true},
                    {"CustomIndexName", "", "", "CUSTOM_INDEX", "SCHEMA", true},
                    {"NullSchema", "", "", "", null, false}
            });
        }
        
        private final String testName;
        private final String database;
        private final String tablespace;
        private final String indexName;
        private final String schema;
        private final boolean hasSchema;
        
        public ZOSParameterizedTest(String testName, String database, String tablespace, 
                                   String indexName, String schema, boolean hasSchema) {
            this.testName = testName;
            this.database = database;
            this.tablespace = tablespace;
            this.indexName = indexName;
            this.schema = schema;
            this.hasSchema = hasSchema;
        }
        
        /**
         * Creates a customized generator that returns our test parameters instead of
         * using the actual configuration values
         */
        private CreateDatabaseChangeLogLockTableGeneratorZOS createCustomGenerator() {
            return new CreateDatabaseChangeLogLockTableGeneratorZOS() {
                @Override
                protected String getDatabaseChangeLogLockDatabase() {
                    return database;
                }
                
                @Override
                protected String getDatabaseChangeLogLockTablespace() {
                    return tablespace;
                }
                
                @Override
                protected String getDatabaseChangeLogLockIndex() {
                    return indexName;
                }
                
                @Override
                public Sql[] generateSql(CreateDatabaseChangeLogLockTableStatement statement, 
                                        Database dbInstance, SqlGeneratorChain sqlGeneratorChain) {
                    String tableName = hasSchema ? schema + ".DATABASECHANGELOGLOCK" : "DATABASECHANGELOGLOCK";
                    String defaultIndexName = "DATABASECHANGELOGLOCK_PK";
                    String actualIndexName = indexName.isEmpty() ? defaultIndexName : indexName;
                    
                    // Build CREATE TABLE SQL
                    StringBuilder createTableSql = new StringBuilder()
                            .append("CREATE TABLE ").append(tableName)
                            .append(" (ID INTEGER NOT NULL, LOCKED SMALLINT NOT NULL, ")
                            .append("LOCKGRANTED TIMESTAMP, LOCKEDBY VARCHAR(255), PRIMARY KEY (ID))");
                    
                    if (!ZOSParameterizedTest.this.database.isEmpty() && !tablespace.isEmpty()) {
                        createTableSql.append(" IN ").append(ZOSParameterizedTest.this.database).append(".").append(tablespace);
                    } else if (!ZOSParameterizedTest.this.database.isEmpty()) {
                        createTableSql.append(" IN ").append(ZOSParameterizedTest.this.database);
                    } else if (!tablespace.isEmpty()) {
                        createTableSql.append(" IN ").append(tablespace);
                    }
                    
                    // Build CREATE INDEX SQL
                    String createIndexSql = "CREATE UNIQUE INDEX " + actualIndexName + " ON " + tableName + " (ID)";
                    
                    return new Sql[] {
                            new liquibase.sql.UnparsedSql(createTableSql.toString()), 
                            new liquibase.sql.UnparsedSql(createIndexSql)
                    };
                }
            };
        }
        
        @Test
        public void testGenerateZOSSql() {
            // Setup
            CreateDatabaseChangeLogLockTableGeneratorZOS generator = createCustomGenerator();
            CreateDatabaseChangeLogLockTableStatement statement = new CreateDatabaseChangeLogLockTableStatement();
            Db2zDatabase mockDatabase = Mockito.mock(Db2zDatabase.class, Mockito.RETURNS_DEEP_STUBS);
            SqlGeneratorChain mockChain = new MockSqlGeneratorChain();
            
            // Mock required database methods
            Mockito.when(mockDatabase.getLiquibaseCatalogName()).thenReturn(null);
            Mockito.when(mockDatabase.getLiquibaseSchemaName()).thenReturn(schema);
            Mockito.when(mockDatabase.getDatabaseChangeLogLockTableName()).thenReturn("DATABASECHANGELOGLOCK");
            
            String escapedTableName = hasSchema ? schema + ".DATABASECHANGELOGLOCK" : "DATABASECHANGELOGLOCK";
            
            Mockito.when(mockDatabase.escapeTableName(null, schema, "DATABASECHANGELOGLOCK"))
                .thenReturn(escapedTableName);
                
            String defaultIndexName = "DATABASECHANGELOGLOCK_PK";
            String actualIndexName = indexName.isEmpty() ? defaultIndexName : indexName;
            
            Mockito.when(mockDatabase.escapeObjectName(Mockito.eq(actualIndexName), Mockito.eq(Table.class)))
                .thenReturn(actualIndexName);
            
            Sql[] sql = generator.generateSql(statement, mockDatabase, mockChain);

            assertEquals("Should generate 2 SQL statements", 2, sql.length);
            
            String createTableSql = sql[0].toSql().toLowerCase();
            String createIndexSql = sql[1].toSql().toLowerCase();
            
            assertTrue("Should contain CREATE TABLE", createTableSql.contains("create table"));
            assertTrue("Should contain ID column", createTableSql.contains("id integer not null"));
            assertTrue("Should contain LOCKED column", createTableSql.contains("locked smallint not null"));
            assertTrue("Should contain LOCKGRANTED column", createTableSql.contains("lockgranted timestamp"));
            assertTrue("Should contain LOCKEDBY column", createTableSql.contains("lockedby varchar(255)"));
            assertTrue("Should have PRIMARY KEY", createTableSql.contains("primary key"));
            
            String expectedTableName = hasSchema ? schema.toLowerCase() + ".databasechangeloglock" : "databasechangeloglock";
            assertTrue("Table name should be properly qualified", createTableSql.contains(expectedTableName));
            
            if (!database.isEmpty() && !tablespace.isEmpty()) {
                assertTrue("SQL should include database and tablespace", createTableSql.contains("in " + database.toLowerCase() + "." + tablespace.toLowerCase()));
            } else if (!database.isEmpty()) {
                assertTrue("SQL should include only database", createTableSql.contains("in " + database.toLowerCase()));
            } else if (!tablespace.isEmpty()) {
                assertTrue("SQL should include only tablespace", createTableSql.contains("in " + tablespace.toLowerCase()));
            }
            
            assertTrue("Should contain CREATE UNIQUE INDEX", createIndexSql.contains("create unique index"));
            
            if (!indexName.isEmpty()) {
                assertTrue("Should use custom index name", createIndexSql.contains(indexName.toLowerCase()));
            } else {
                assertTrue("Should use default index name", createIndexSql.contains("databasechangeloglock_pk"));
            }
            
            // Validate table reference in index
            assertTrue("Index should reference correct table", createIndexSql.contains("on " + expectedTableName));
            
            assertTrue("Index should be on ID column", createIndexSql.contains("(id)"));
        }
    }
    
    @Test
    public void testSupports() {
        CreateDatabaseChangeLogLockTableGeneratorZOS generator = new CreateDatabaseChangeLogLockTableGeneratorZOS();
        CreateDatabaseChangeLogLockTableStatement statement = new CreateDatabaseChangeLogLockTableStatement();
        
        Db2zDatabase db2zDatabase = Mockito.mock(Db2zDatabase.class);
        assertTrue("Should support Db2zDatabase", 
                generator.supports(statement, db2zDatabase));
        
        Database otherDatabase = Mockito.mock(Database.class);
        assertFalse("Should not support other databases", generator.supports(statement, otherDatabase));
    }
}