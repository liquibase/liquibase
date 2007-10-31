package liquibase.database.sql;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.database.template.JdbcTemplate;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.TestContext;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class ReorganizeTableStatementTest extends AbstractSqlStatementTest {
    private static final String TABLE_NAME = "AddReorgTableTest";

    @Before
    public void dropAndCreateTable() throws Exception {
        for (Database database : TestContext.getInstance().getAvailableDatabases()) {
            if (new ReorganizeTableStatement(null, null).supportsDatabase(database)) {

                dropAndCreateTable(new CreateTableStatement(TABLE_NAME).addColumn("existingCol", "int"), database);

                if (database.supportsSchemas()) {
                    dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME).addColumn("existingCol", "int"), database);
                }
            }
        }

    }

    protected SqlStatement generateTestStatement() {
        return new ReorganizeTableStatement(null, null);
    }

    @Test
    public void supportsDatabase() throws Exception {
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                if (database instanceof DB2Database) {
                    assertTrue(new ReorganizeTableStatement(null, null).supportsDatabase(database));
                } else {
                    assertFalse(new ReorganizeTableStatement(null, null).supportsDatabase(database));
                }
            }
        });
    }

    @Test
    public void execute_noSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                ReorganizeTableStatement statement = new ReorganizeTableStatement(null, TABLE_NAME);
                if (statement.supportsDatabase(database)) {
                    new JdbcTemplate(database).execute(statement);
                } else {
                    try {
                        statement.getSqlStatement(database);
                        fail("Should have thrown exception");
                    } catch (StatementNotSupportedOnDatabaseException e) {
                        assertEquals("Cannot reorganize table", e.getReason());
                    }
                }
            }
        });
    }

    @Test
    public void execute_withSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                ReorganizeTableStatement statement = new ReorganizeTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME);
                if (statement.supportsDatabase(database)) {
                    new JdbcTemplate(database).execute(statement);
                } else {
                    try {
                        statement.getSqlStatement(database);
                        fail("Should have thrown exception");
                    } catch (StatementNotSupportedOnDatabaseException e) {
                        assertEquals("Cannot reorganize table", e.getReason());
                    }
                }
            }
        });
    }
}
