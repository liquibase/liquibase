package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.DB2Database;
import liquibase.database.core.Db2zDatabase;
import liquibase.exception.DatabaseException;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
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
                    {"WithSchema", "LIQUIBASE", null, true, 
                     "CREATE TABLE \"LIQUIBASE\".DATABASECHANGELOGLOCK (ID INTEGER NOT NULL, LOCKED SMALLINT NOT NULL, LOCKGRANTED TIMESTAMP, LOCKEDBY VARCHAR(255), CONSTRAINT PK_DBCHGLOGLOCK PRIMARY KEY (ID))"},
                    {"WithoutSchema", null, null, false, 
                     "CREATE TABLE DATABASECHANGELOGLOCK (ID INTEGER NOT NULL, LOCKED SMALLINT NOT NULL, LOCKGRANTED TIMESTAMP, LOCKEDBY VARCHAR(255), CONSTRAINT PK_DBCHGLOGLOCK PRIMARY KEY (ID))"},
                    {"WithTablespace", "LIQUIBASE", "LIQUIBASE_TS", true, 
                     "CREATE TABLE \"LIQUIBASE\".DATABASECHANGELOGLOCK (ID INTEGER NOT NULL, LOCKED SMALLINT NOT NULL, LOCKGRANTED TIMESTAMP, LOCKEDBY VARCHAR(255), CONSTRAINT PK_DBCHGLOGLOCK PRIMARY KEY (ID)) IN LIQUIBASE_TS"}
            });
        }

        private final String testName;
        private final String schema;
        private final String tablespace;
        private final boolean hasSchema;
        private final String expectedSql;

        public DB2ParameterizedTest(String testName, String schema, String tablespace, boolean hasSchema, String expectedSql) {
            this.testName = testName;
            this.schema = schema;
            this.tablespace = tablespace;
            this.hasSchema = hasSchema;
            this.expectedSql = expectedSql;
        }

        @Test
        public void testGenerateSqlForDB2() throws DatabaseException {
            CreateDatabaseChangeLogLockTableStatement statement = new CreateDatabaseChangeLogLockTableStatement();
            
            DB2Database database = new DB2Database() {
                @Override
                public String getLiquibaseSchemaName() {
                    return schema;
                }
                
                @Override
                public String getLiquibaseTablespaceName() {
                    return tablespace;
                }
                
                @Override
                public boolean isCorrectDatabaseImplementation(liquibase.database.DatabaseConnection conn) {
                    return true;
                }
                
                @Override
                public boolean requiresUsername() {
                    return false;
                }
                
                @Override
                public boolean requiresPassword() {
                    return false;
                }
            };
            
            CreateDatabaseChangeLogLockTableGenerator generator = new CreateDatabaseChangeLogLockTableGenerator();
            SqlGeneratorChain chain = new SqlGeneratorChain(SqlGeneratorFactory.getInstance().getGenerators(statement, database));
            
            Sql[] sql = generator.generateSql(statement, database, chain);
            
            assertSqlBasicStructure(sql);

            String actualSql = sql[0].toSql();
            
            assertEquals("Generated SQL should exactly match expected SQL", expectedSql, actualSql);

            String lowerActualSql = actualSql.toLowerCase();

            if (hasSchema) {
                if (schema != null) {
                    assertTrue("SQL should contain schema", lowerActualSql.contains(schema.toLowerCase()) || lowerActualSql.contains(database.getDefaultSchemaName().toLowerCase()));
                }
            }

            if (tablespace != null && database.supportsTablespaces()) {
                assertTrue("SQL should include tablespace", lowerActualSql.contains("in " + tablespace.toLowerCase()));
            }
        }

        private void assertSqlBasicStructure(Sql[] sql) {
            assertTrue("SQL should be generated", sql != null && sql.length > 0);

            String actualSql = sql[0].toSql().toLowerCase();

            assertTrue("Should contain CREATE TABLE", actualSql.contains("create table"));
            assertTrue("Should contain DATABASECHANGELOGLOCK", actualSql.contains("databasechangeloglock"));
            assertTrue("Should have ID column", actualSql.contains("id") && actualSql.contains("integer"));
            assertTrue("Should have LOCKED column", actualSql.contains("locked") && actualSql.contains("smallint"));
            assertTrue("Should have LOCKGRANTED column", actualSql.contains("lockgranted"));
            assertTrue("Should have LOCKEDBY column", actualSql.contains("lockedby") && actualSql.contains("varchar"));
            assertTrue("Should have PRIMARY KEY", actualSql.contains("primary key") || actualSql.contains("constraint"));
        }
    }
}