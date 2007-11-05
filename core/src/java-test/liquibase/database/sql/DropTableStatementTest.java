package liquibase.database.sql;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.database.DerbyDatabase;
import liquibase.database.FirebirdDatabase;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.exception.JDBCException;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Test;

public class DropTableStatementTest extends AbstractSqlStatementTest {
    private static final String TABLE_NAME = "DropTableTest";

    protected void setupDatabase(Database database) throws Exception {
        dropAndCreateTable(new CreateTableStatement(TABLE_NAME)
                .addPrimaryKeyColumn("id", "int")
                , database);

        dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                .addPrimaryKeyColumn("id", "int")
                , database);
    }

    protected SqlStatement generateTestStatement() {
        return new DropTableStatement(null, null, true);
    }

    @Test
    public void execute_defaultSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new DropTableStatement(null, TABLE_NAME, false)) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getTable(TABLE_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME));
                    }

                });
    }

    @Test
    public void execute_cascadeConstraints() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new DropTableStatement(null, TABLE_NAME, true)) {

                    protected boolean expectedException(Database database, JDBCException exception) {
                        return database instanceof DerbyDatabase
                                || database instanceof DB2Database
                                || database instanceof FirebirdDatabase;
                    }
                    
                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getTable(TABLE_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME));
                    }

                });
    }

    @Test
    public void execute_altSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new DropTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME, false)) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getTable(TABLE_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME));
                    }

                });
    }
}
