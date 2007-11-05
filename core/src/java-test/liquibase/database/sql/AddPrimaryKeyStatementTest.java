package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.template.JdbcTemplate;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import liquibase.exception.JDBCException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class AddPrimaryKeyStatementTest extends AbstractSqlStatementTest {

    private static final String TABLE_NAME = "AddPKTest";
    private static final String COLUMN_NAME = "id";
    private static final String COLUMN2_NAME = "id2";

    protected void setupDatabase(Database database) throws Exception {
            dropAndCreateTable(new CreateTableStatement(TABLE_NAME)
                    .addColumn(COLUMN_NAME, "int", new NotNullConstraint())
                    .addColumn(COLUMN2_NAME, "int", new NotNullConstraint()), database);

            dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                    .addColumn(COLUMN_NAME, "int", new NotNullConstraint())
                    .addColumn(COLUMN2_NAME, "int", new NotNullConstraint()), database);
    }

    protected SqlStatement generateTestStatement() {
        return new AddPrimaryKeyStatement(null, null, null, null);
    }

    @Test
    public void execute_noSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new AddPrimaryKeyStatement(null, TABLE_NAME, COLUMN_NAME, "PK_addpktest")) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());
                    }

                });
    }

    @Test
    public void execute_altSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new AddPrimaryKeyStatement(TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME, "PK_addpktest")) {
                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());
                    }

                });
    }

    @Test
    public void execute_compundPKNoSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new AddPrimaryKeyStatement(null, TABLE_NAME, COLUMN_NAME + "," + COLUMN2_NAME, "PK_addpktest")) {
                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
                        assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());
                    }

                });
    }

    @Test
    public void execute_compundPKAltSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new AddPrimaryKeyStatement(TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME + "," + COLUMN2_NAME, "PK_addpktest")) {
                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
                        assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());
                    }
                });
    }

    @Test
    public void execute_withTablespace() throws Exception {

        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new AddPrimaryKeyStatement(null, TABLE_NAME, COLUMN_NAME, "PK_addpktest").setTablespace(TestContext.ALT_TABLESPACE)) {

                    protected boolean expectedException(Database database, JDBCException exception) {
                        return !database.supportsTablespaces();
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());
                    }

                });
    }
}
