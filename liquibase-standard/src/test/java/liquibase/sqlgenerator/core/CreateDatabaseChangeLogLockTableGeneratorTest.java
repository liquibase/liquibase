package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.Db2zDatabase;
import liquibase.exception.DatabaseException;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class CreateDatabaseChangeLogLockTableGeneratorTest extends AbstractSqlGeneratorTest<CreateDatabaseChangeLogLockTableStatement> {

    public CreateDatabaseChangeLogLockTableGeneratorTest() throws Exception {
        super(new CreateDatabaseChangeLogLockTableGenerator());
    }

    @Override
    protected CreateDatabaseChangeLogLockTableStatement createSampleSqlStatement() {
        return new CreateDatabaseChangeLogLockTableStatement();
    }

    @Override
    protected boolean shouldBeImplementation(Database database) {
        return !(database instanceof Db2zDatabase);
    }

    @RunWith(Parameterized.class)
    public static class DB2ParameterizedTest {

        @Parameters(name = "{0}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {"WithSchema", "LIQUIBASE", null, true},
                    {"WithoutSchema", null, null, false},
                    {"WithTablespace", "LIQUIBASE", "LIQUIBASE_TS", true}
            });
        }

        private final String testName;
        private final String schema;
        private final String tablespace;
        private final boolean hasSchema;

        public DB2ParameterizedTest(String testName, String schema, String tablespace, boolean hasSchema) {
            this.testName = testName;
            this.schema = schema;
            this.tablespace = tablespace;
            this.hasSchema = hasSchema;
        }

        @Test
        public void testGenerateSqlForDB2() throws DatabaseException {
            CreateDatabaseChangeLogLockTableGenerator generator = new CreateDatabaseChangeLogLockTableGenerator() {
                @Override
                public Sql[] generateSql(CreateDatabaseChangeLogLockTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
                    String schemaPrefix = schema != null ? schema + "." : "";
                    String tablespaceClause = tablespace != null ? " IN " + tablespace : "";
                    
                    return new Sql[] {
                        new liquibase.sql.UnparsedSql(
                            "CREATE TABLE " + schemaPrefix + "DATABASECHANGELOGLOCK " +
                            "(ID INTEGER NOT NULL, LOCKED SMALLINT NOT NULL, " +
                            "LOCKGRANTED TIMESTAMP, LOCKEDBY VARCHAR(255), " +
                            "CONSTRAINT PK_DBCHGLOGLOCK PRIMARY KEY (ID))" + tablespaceClause
                        )
                    };
                }
            };
            
            CreateDatabaseChangeLogLockTableStatement statement = new CreateDatabaseChangeLogLockTableStatement();
            SqlGeneratorChain mockChain = Mockito.mock(SqlGeneratorChain.class);

            DB2Database database = configureMockDB2Database(schema, tablespace);
            
            Sql[] sql = generator.generateSql(statement, database, mockChain);
            
            assertSqlBasicStructure(sql);

            String actualSql = sql[0].toSql().toLowerCase();

            if (hasSchema) {
                assertTrue("SQL should contain schema.table",
                        actualSql.contains((schema != null ? schema.toLowerCase() + "." : "") +
                                "databasechangeloglock"));
            }

            if (tablespace != null) {
                assertTrue("SQL should include tablespace",
                        actualSql.contains("tablespace " + tablespace.toLowerCase()) ||
                                actualSql.contains("in " + tablespace.toLowerCase()));
            }
        }

        private DB2Database configureMockDB2Database(String schema, String tablespace) throws DatabaseException {
            DB2Database database = Mockito.mock(DB2Database.class, Mockito.RETURNS_DEEP_STUBS);

            // Basic configuration
            Mockito.when(database.getLiquibaseCatalogName()).thenReturn(null);
            Mockito.when(database.getLiquibaseSchemaName()).thenReturn(schema);
            Mockito.when(database.getLiquibaseTablespaceName()).thenReturn(tablespace);
            Mockito.when(database.getDatabaseChangeLogLockTableName()).thenReturn("DATABASECHANGELOGLOCK");

            String tableName = schema != null ? schema + ".DATABASECHANGELOGLOCK" : "DATABASECHANGELOGLOCK";
            Mockito.when(database.escapeTableName(null, schema, "DATABASECHANGELOGLOCK"))
                    .thenReturn(tableName);

            Mockito.when(database.escapeColumnName(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.eq("ID"))).thenReturn("ID");
            Mockito.when(database.escapeColumnName(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.eq("LOCKED"))).thenReturn("LOCKED");
            Mockito.when(database.escapeColumnName(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.eq("LOCKGRANTED"))).thenReturn("LOCKGRANTED");
            Mockito.when(database.escapeColumnName(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.eq("LOCKEDBY"))).thenReturn("LOCKEDBY");

            Mockito.when(database.escapeObjectName(Mockito.anyString(), Mockito.eq(Table.class)))
                    .thenAnswer(invocation -> {
                        String objName = invocation.getArgument(0);
                        if ("pk_databasechangeloglock".equals(objName)) {
                            return "PK_DBCHGLOGLOCK";
                        }
                        return objName;
                    });

            Mockito.when(database.getObjectQuotingStrategy()).thenReturn(null);
            Mockito.when(database.isAutoCommit()).thenReturn(true);
            Mockito.when(database.supportsTablespaces()).thenReturn(true);
            
            return database;
        }

        private void assertSqlBasicStructure(Sql[] sql) {
            assertTrue("SQL should be generated", sql != null && sql.length > 0);

            String actualSql = sql[0].toSql().toLowerCase();

            assertTrue("Should contain CREATE TABLE", actualSql.contains("create table"));
            assertTrue("Should contain DATABASECHANGELOGLOCK", actualSql.contains("databasechangeloglock"));
            assertTrue("Should have ID column", actualSql.contains("id") && actualSql.contains("integer"));
            assertTrue("Should have LOCKED column", actualSql.contains("locked") && actualSql.contains("smallint"));
            assertTrue("Should have LOCKGRANTED column", actualSql.contains("lockgranted") && actualSql.contains("timestamp"));
            assertTrue("Should have LOCKEDBY column", actualSql.contains("lockedby") && actualSql.contains("varchar"));
            assertTrue("Should have PRIMARY KEY", actualSql.contains("primary key") || actualSql.contains("constraint"));
        }
    }

    @Test
    public void testValidate() throws DatabaseException {
        CreateDatabaseChangeLogLockTableGenerator generator = new CreateDatabaseChangeLogLockTableGenerator();
        CreateDatabaseChangeLogLockTableStatement statement = new CreateDatabaseChangeLogLockTableStatement();
        SqlGeneratorChain mockChain = Mockito.mock(SqlGeneratorChain.class);

        DB2Database database = Mockito.mock(DB2Database.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(database.isAutoCommit()).thenReturn(true);
        Mockito.when(database.supportsSchemas()).thenReturn(true);
        Mockito.when(database.supportsTablespaces()).thenReturn(true);

        assertFalse("Validation should not have errors", generator.validate(statement, database, mockChain).hasErrors());
    }
}