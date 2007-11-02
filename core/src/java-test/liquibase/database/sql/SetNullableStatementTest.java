package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.FirebirdDatabase;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.template.JdbcTemplate;
import liquibase.exception.JDBCException;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.TestContext;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class SetNullableStatementTest extends AbstractSqlStatementTest {

    private static final String NULLABLE_TABLE_NAME = "DropNotNullTest";
    private static final String NOTNULL_TABLE_NAME = "AddNotNullTest";
    private static final String COLUMN_NAME = "testCol";

    @Before
    public void dropTable() throws Exception {
        for (Database database : TestContext.getInstance().getAvailableDatabases()) {

            dropAndCreateTable(new CreateTableStatement(null, NOTNULL_TABLE_NAME)
                        .addColumn("id", "int")
                        .addColumn(COLUMN_NAME, "varchar(50)"), database);

            dropAndCreateTable(new CreateTableStatement(null, NULLABLE_TABLE_NAME)
                        .addColumn("id", "int")
                        .addColumn(COLUMN_NAME, "varchar(50)", new NotNullConstraint()), database);

            if (database.supportsSchemas()) {
                dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, NOTNULL_TABLE_NAME)
                            .addColumn("id", "int")
                            .addColumn(COLUMN_NAME, "varchar(50)"), database);

                dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, NULLABLE_TABLE_NAME)
                            .addColumn("id", "int")
                            .addColumn(COLUMN_NAME, "varchar(50)", new NotNullConstraint()), database);
            }
        }

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
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                SetNullableStatement statement = new SetNullableStatement(null, NOTNULL_TABLE_NAME, COLUMN_NAME, "varchar(50)", false);
                if (!statement.supportsDatabase(database)) {
                    try {
                        statement.getSqlStatement(database);
                        fail("Did not throw exception");
                    } catch (StatementNotSupportedOnDatabaseException e) {
                        ; //what we wanted
                    }
                    return;
                }

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertTrue(snapshot.getTable(NOTNULL_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());

                new JdbcTemplate(database).execute(statement);

                snapshot = new DatabaseSnapshot(database);
                assertFalse(snapshot.getTable(NOTNULL_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
            }
        });
    }

    @Test
    public void execute_withSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                if (!database.supportsSchemas()) {
                    return;
                }

                SetNullableStatement statement = new SetNullableStatement(TestContext.ALT_SCHEMA, NOTNULL_TABLE_NAME, COLUMN_NAME, "varchar(50)", false);

                if (!statement.supportsDatabase(database)) {
                    try {
                        statement.getSqlStatement(database);
                        fail("Did not throw exception");
                    } catch (StatementNotSupportedOnDatabaseException e) {
                        ; //what we wanted
                    }
                    return;
                }

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
                assertTrue(snapshot.getTable(NOTNULL_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());

                new JdbcTemplate(database).execute(statement);

                snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
                assertFalse(snapshot.getTable(NOTNULL_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
            }
        });
    }

    @Test
    public void execute_nowNullNoSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                SetNullableStatement statement = new SetNullableStatement(null, NULLABLE_TABLE_NAME, COLUMN_NAME, "varchar(50)", true);
                if (!statement.supportsDatabase(database)) {
                    try {
                        statement.getSqlStatement(database);
                        fail("Did not throw exception");
                    } catch (StatementNotSupportedOnDatabaseException e) {
                        ; //what we wanted
                    }
                    return;
                }

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertFalse(snapshot.getTable(NULLABLE_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());

                new JdbcTemplate(database).execute(statement);

                snapshot = new DatabaseSnapshot(database);
                assertTrue(snapshot.getTable(NULLABLE_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
            }
        });
    }

    @Test
    public void execute_nowNullableWithSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                if (!database.supportsSchemas()) {
                    return;
                }

                SetNullableStatement statement = new SetNullableStatement(TestContext.ALT_SCHEMA, NULLABLE_TABLE_NAME, COLUMN_NAME, "varchar(50)", true);

                if (!statement.supportsDatabase(database)) {
                    try {
                        statement.getSqlStatement(database);
                        fail("Did not throw exception");
                    } catch (StatementNotSupportedOnDatabaseException e) {
                        ; //what we wanted
                    }
                    return;
                }

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
                assertFalse(snapshot.getTable(NULLABLE_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());

                new JdbcTemplate(database).execute(statement);

                snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
                assertTrue(snapshot.getTable(NULLABLE_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
            }
        });
    }
}
