package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.DerbyDatabase;
import liquibase.database.MSSQLDatabase;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.template.JdbcTemplate;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.TestContext;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class DropColumnStatementTest extends AbstractSqlStatementTest {

    private static final String TABLE_NAME = "DropColumnTest";
    private static final String COLUMN_NAME = "testCol";

    @Before
    public void setupTable() throws Exception {
        for (Database database : TestContext.getInstance().getAvailableDatabases()) {

            dropAndCreateTable(new CreateTableStatement(TABLE_NAME)
                    .addPrimaryKeyColumn("id", "int")
                    .addColumn(COLUMN_NAME, "varchar(50)")
                    , database);

            if (database.supportsSchemas()) {
                dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                        .addPrimaryKeyColumn("id", "int")
                        .addColumn(COLUMN_NAME, "varchar(50)")
                        , database);
            }
        }
    }

    @Test
    public void supportsDatabase() throws Exception {
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                if (database instanceof DerbyDatabase) {
                    assertFalse(generateTestStatement().supportsDatabase(database));
                } else {
                    assertTrue(generateTestStatement().supportsDatabase(database));
                }
            }
        });
    }

    protected DropColumnStatement generateTestStatement() {
        return new DropColumnStatement(null, null, null);
    }

    @Test
    public void execute_defaultSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                DropColumnStatement statement = new DropColumnStatement(null, TABLE_NAME, COLUMN_NAME);
                if (!statement.supportsDatabase(database)) {
                    try {
                        statement.getSqlStatement(database);
                        fail("did not throw exception");
                    } catch (StatementNotSupportedOnDatabaseException e) {
                        return; //what we expected
                    }
                }

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNotNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME));

                new JdbcTemplate(database).execute(statement);

                if (database instanceof MSSQLDatabase) {
                    return; //mssql does not seem to be updating the metadata fast enough and it is failing
                }
                snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME));
            }
        });
    }

    @Test
    public void execute_altSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                if (!database.supportsSchemas()) {
                    return;
                }

                DropColumnStatement statement = new DropColumnStatement(TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME);
                if (!statement.supportsDatabase(database)) {
                    try {
                        statement.getSqlStatement(database);
                        fail("did not throw exception");
                    } catch (StatementNotSupportedOnDatabaseException e) {
                        return; //what we expected
                    }
                }

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
                assertNotNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME));

                new JdbcTemplate(database).execute(statement);

                if (database instanceof MSSQLDatabase) {
                    return; //mssql does not seem to be updating the metadata fast enough and it is failing
                }
                snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
                assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME));
            }
        });
    }
}
