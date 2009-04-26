package liquibase.database.statement;

import liquibase.database.Database;
import liquibase.database.statement.CreateTableStatement;
import liquibase.database.statement.SqlStatement;
import liquibase.database.statement.UpdateStatement;
import liquibase.database.statement.AbstractSqStatementTest;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import org.junit.Test;

public class UpdateGeneratorTest extends AbstractSqStatementTest {

    private static final String TABLE_NAME = "UpdateTest";
    private static final String COLUMN_NAME = "testCol";

    protected void setupDatabase(Database database) throws Exception {
        dropAndCreateTable(new CreateTableStatement(null, TABLE_NAME)
                .addColumn("id", "int")
                .addColumn(COLUMN_NAME, "varchar(50)"), database);

        dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                .addColumn("id", "int")
                .addColumn(COLUMN_NAME, "varchar(50)"), database);
    }

    protected SqlStatement createGeneratorUnderTest() {
        return new UpdateStatement(null, null);
    }

//    @Test
//    public void addNewColumnValue_nullValue() throws Exception {
//        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
//
//            public void performTest(Database database) {
//                UpdateStatement statement = new UpdateStatement(null, TABLE_NAME);
//                statement.addNewColumnValue(COLUMN_NAME, null);
//
//                assertEquals("UPDATE " + database.escapeTableName(null, TABLE_NAME) + " SET " + database.escapeColumnName(null, TABLE_NAME, COLUMN_NAME) + " = NULL", statement.getSqlStatement(database));
//            }
//        });
//    }

    @Test
    public void execute_altSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA,
                new UpdateStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                        .addNewColumnValue(COLUMN_NAME, null)) {
            protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                //nothing to test
            }

            protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                //nothing to test
            }
        });
    }
}
