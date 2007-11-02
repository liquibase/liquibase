package liquibase.database.sql;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import liquibase.database.Database;
import liquibase.database.DerbyDatabase;
import liquibase.database.MSSQLDatabase;
import liquibase.database.template.JdbcTemplate;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.test.TestContext;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.DatabaseTest;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class DropDefaultValueStatementTest extends AbstractSqlStatementTest {
    private static final String TABLE_NAME = "DropDefaultTest";
    private static final String COLUMN_NAME = "testCol";

    @Before
    public void setupTable() throws Exception {
        for (Database database : TestContext.getInstance().getAvailableDatabases()) {

            dropAndCreateTable(new CreateTableStatement(TABLE_NAME)
                    .addPrimaryKeyColumn("id", "int")
                    .addColumn(COLUMN_NAME, "varchar(50)", "'Def Value'")
                    , database);

            if (database.supportsSchemas()) {
                dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                        .addPrimaryKeyColumn("id", "int")
                        .addColumn(COLUMN_NAME, "varchar(50)", "'Def Value'")
                        , database);
            }
        }
    }

    protected DropDefaultValueStatement generateTestStatement() {
        return new DropDefaultValueStatement(null, null, null);
    }

    @Test
    public void execute_defaultSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                DropDefaultValueStatement statement = new DropDefaultValueStatement(null, TABLE_NAME, COLUMN_NAME);

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNotNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());

                new JdbcTemplate(database).execute(statement);

                if (database instanceof DerbyDatabase) {
                    return; //meatadata not updating
                }
                snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());
            }
        });
    }
    
    @Test
    public void execute_altSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                DropDefaultValueStatement statement = new DropDefaultValueStatement(TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME);

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
                assertNotNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());

                new JdbcTemplate(database).execute(statement);

                if (database instanceof DerbyDatabase) {
                    return; //meatadata not updating
                }
                snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
                assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());
            }
        });
    }
}
