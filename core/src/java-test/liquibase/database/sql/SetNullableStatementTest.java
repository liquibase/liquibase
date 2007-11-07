package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.FirebirdDatabase;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Test;

public class SetNullableStatementTest extends AbstractSqlStatementTest {

    private static final String NULLABLE_TABLE_NAME = "DropNotNullTest";
    private static final String NOTNULL_TABLE_NAME = "AddNotNullTest";
    private static final String COLUMN_NAME = "testCol";

    protected void setupDatabase(Database database) throws Exception {
        dropAndCreateTable(new CreateTableStatement(null, NOTNULL_TABLE_NAME)
                .addColumn("id", "int")
                .addColumn(COLUMN_NAME, "varchar(50)"), database);

        dropAndCreateTable(new CreateTableStatement(null, NULLABLE_TABLE_NAME)
                .addColumn("id", "int")
                .addColumn(COLUMN_NAME, "varchar(50)", new NotNullConstraint()), database);

        dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, NOTNULL_TABLE_NAME)
                .addColumn("id", "int")
                .addColumn(COLUMN_NAME, "varchar(50)"), database);

        dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, NULLABLE_TABLE_NAME)
                .addColumn("id", "int")
                .addColumn(COLUMN_NAME, "varchar(50)", new NotNullConstraint()), database);
    }

    protected SetNullableStatement generateTestStatement() {
        return new SetNullableStatement(null, null, null, null, true);
    }

    @Test
    public void supportsDatabase() throws Exception {
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                SetNullableStatement statement = generateTestStatement();

                if (database instanceof FirebirdDatabase) {
                    assertFalse(statement.supportsDatabase(database));
                } else {
                    assertTrue(statement.supportsDatabase(database));
                }
            }
        });
    }

    @Test
    public void execute_nowNotNullNoSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new SetNullableStatement(null, NOTNULL_TABLE_NAME, COLUMN_NAME, "varchar(50)", false)) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertTrue(snapshot.getTable(NOTNULL_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertFalse(snapshot.getTable(NOTNULL_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
                    }
                });
    }

    @Test
    public void execute_withSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new SetNullableStatement(TestContext.ALT_SCHEMA, NOTNULL_TABLE_NAME, COLUMN_NAME, "varchar(50)", false)) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertTrue(snapshot.getTable(NOTNULL_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertFalse(snapshot.getTable(NOTNULL_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
                    }
                });
    }

    @Test
    public void execute_nowNullNoSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new SetNullableStatement(null, NULLABLE_TABLE_NAME, COLUMN_NAME, "varchar(50)", true)) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertFalse(snapshot.getTable(NULLABLE_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertTrue(snapshot.getTable(NULLABLE_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
                    }
                });
    }

    @Test
    public void execute_nowNullableWithSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new SetNullableStatement(TestContext.ALT_SCHEMA, NULLABLE_TABLE_NAME, COLUMN_NAME, "varchar(50)", true)) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertFalse(snapshot.getTable(NULLABLE_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertTrue(snapshot.getTable(NULLABLE_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
                    }
                });
    }
}
