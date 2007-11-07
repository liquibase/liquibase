package liquibase.database.sql;

import liquibase.database.*;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Test;

public class AddAutoIncrementStatementTest extends AbstractSqlStatementTest {

    private static final String TABLE_NAME = "AddAutoIncTest";
    private static final String COLUMN_NAME = "testCol";
    private static final String COLUMN_TYPE = "int";

    protected void setupDatabase(Database database) throws Exception {
        dropAndCreateTable(new CreateTableStatement(TABLE_NAME)
                .addPrimaryKeyColumn(COLUMN_NAME, COLUMN_TYPE)
                .addColumn("otherColumn", "varchar(50)"), database);

        dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                .addPrimaryKeyColumn(COLUMN_NAME, COLUMN_TYPE)
                .addColumn("otherColumn", "varchar(50)"), database);
    }

    protected AddAutoIncrementStatement generateTestStatement() {
        return new AddAutoIncrementStatement(null, null, null, null);
    }

    @Test
    public void supportsDatabase() throws Exception {
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                if (database instanceof OracleDatabase
                        || database instanceof MSSQLDatabase
                        || database instanceof PostgresDatabase
                        || database instanceof DerbyDatabase
                        || database instanceof CacheDatabase
                        || database instanceof H2Database
                        || database instanceof FirebirdDatabase) {
                    assertFalse(generateTestStatement().supportsDatabase(database));
                } else {
                    assertTrue(generateTestStatement().supportsDatabase(database));
                }
            }

        });
    }

    @Test
    public void execute_defaultSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new AddAutoIncrementStatement(null, TABLE_NAME, COLUMN_NAME, COLUMN_TYPE)) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isAutoIncrement());
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isAutoIncrement());
                    }
                });
    }

    @Test
    public void execute_alternateSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new AddAutoIncrementStatement(TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME, COLUMN_TYPE)) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isAutoIncrement());
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isAutoIncrement());
                    }
                });
    }
}
