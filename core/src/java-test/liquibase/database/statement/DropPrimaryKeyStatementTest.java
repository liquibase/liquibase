package liquibase.database.statement;

import liquibase.database.Database;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Test;

public class DropPrimaryKeyStatementTest extends AbstractSqlStatementTest {

    private static final String TABLE_NAME = "DropPKTest";
    private static final String COLUMN_NAME = "id";
    private static final String PK_NAME = "pk_dropPkTest";

    protected void setupDatabase(Database database) throws Exception {
        dropAndCreateTable(new CreateTableStatement(null, TABLE_NAME)
                .addColumn(COLUMN_NAME, "int", new PrimaryKeyConstraint(PK_NAME).addColumns(COLUMN_NAME), new NotNullConstraint())
                .addColumn("otherCol", "varchar(50)")
                , database);

        dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                .addColumn(COLUMN_NAME, "int", new PrimaryKeyConstraint(PK_NAME).addColumns(COLUMN_NAME), new NotNullConstraint())
                .addColumn("otherCol", "varchar(50)")
                , database);
    }

    protected SqlStatement generateTestStatement() {
        return new DropPrimaryKeyStatement(null, null, null);
    }

    @Test
    public void execute_defaultSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new DropPrimaryKeyStatement(null, TABLE_NAME, PK_NAME)) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
                    }

                });
    }

    @Test
    public void execute_altSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new DropPrimaryKeyStatement(TestContext.ALT_SCHEMA, TABLE_NAME, PK_NAME)) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
                    }

                });
    }
}
