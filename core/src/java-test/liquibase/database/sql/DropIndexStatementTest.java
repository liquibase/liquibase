package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.template.JdbcTemplate;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Test;

public class DropIndexStatementTest extends AbstractSqlStatementTest {
    private static final String TABLE_NAME = "DropIndexTest";
    private static final String COLUMN_NAME = "colName";
    private static final String IDX_NAME = "idx_dropindextest";
    private static final String ALT_IDX_NAME = "idx_altdindextest";

    protected void setupDatabase(Database database) throws Exception {
        dropAndCreateTable(new CreateTableStatement(null, TABLE_NAME)
                .addPrimaryKeyColumn("id", "int")
                .addColumn(COLUMN_NAME, "varchar(50)", new NotNullConstraint())
                , database);

        new JdbcTemplate(database).execute(new CreateIndexStatement(IDX_NAME, null, TABLE_NAME, COLUMN_NAME));

        dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                .addPrimaryKeyColumn("id", "int")
                .addColumn(COLUMN_NAME, "varchar(50)", new NotNullConstraint())
                , database);

        if (database.supportsSchemas()) {
            new JdbcTemplate(database).execute(new CreateIndexStatement(ALT_IDX_NAME, TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME));
        }
    }

    protected SqlStatement generateTestStatement() {
        return new DropIndexStatement(null, null, null);
    }

    @Test
    public void execute_defaultSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new DropIndexStatement(IDX_NAME, null, TABLE_NAME)) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getIndex(IDX_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getIndex(IDX_NAME));
                    }

                });
    }

    //todo: issues with schemas on some databases
//    @Test
//    public void execute_altSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new DropIndexStatement(ALT_IDX_NAME, TestContext.ALT_SCHEMA, TABLE_NAME)) {
//
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        //todo: how do we assert indexes within a schema snapshot?
////                        assertNotNull(snapshot.getIndex(ALT_IDX_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        //todo: how do we assert indexes within a schema snapshot?
////                        assertNull(snapshot.getIndex(ALT_IDX_NAME));
//                    }
//
//                });
//    }
}
