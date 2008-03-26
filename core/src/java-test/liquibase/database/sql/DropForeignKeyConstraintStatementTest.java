package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.exception.JDBCException;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Test;

public class DropForeignKeyConstraintStatementTest extends AbstractSqlStatementTest {

    private static final String TABLE_NAME = "DropFKTest";
    private static final String FK_TABLE_NAME = "DropFKTestFK";
    private static final String CONSTRAINT_NAME = "fk_droptest";
    private static final String ALT_SCHEMA_NAME = "ALT" + CONSTRAINT_NAME;

    protected void setupDatabase(Database database) throws Exception {
        dropTableIfExists(null, TABLE_NAME, database);

        dropAndCreateTable(new CreateTableStatement(null, FK_TABLE_NAME)
                .addPrimaryKeyColumn("id", "int", null)
                .addColumn("name", "varchar(50)")
                , database);

        dropAndCreateTable(new CreateTableStatement(null, TABLE_NAME)
                .addPrimaryKeyColumn("id", "int", null)
                .addColumn("test_id", "int", new ForeignKeyConstraint(CONSTRAINT_NAME, FK_TABLE_NAME + "(id)"))
                .addColumn("otherCol", "varchar(50)")
                , database);


        dropTableIfExists(TestContext.ALT_SCHEMA, TABLE_NAME, database);

        dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, FK_TABLE_NAME)
                .addPrimaryKeyColumn("id", "int", null)
                .addColumn("name", "varchar(50)")
                , database);

        dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                .addPrimaryKeyColumn("id", "int", null)
                .addColumn("test_id", "int", new ForeignKeyConstraint(ALT_SCHEMA_NAME, TestContext.ALT_SCHEMA + "." + FK_TABLE_NAME + "(id)"))
                .addColumn("otherCol", "varchar(50)")
                , database);
    }

    protected DropForeignKeyConstraintStatement generateTestStatement() {
        return new DropForeignKeyConstraintStatement(null, null, null);
    }

    @Test
    public void execute_defaultSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new DropForeignKeyConstraintStatement(null, TABLE_NAME, CONSTRAINT_NAME)) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getForeignKey(CONSTRAINT_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getForeignKey(CONSTRAINT_NAME));
                    }

                });
    }

    @Test
    public void execute_altSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases (

                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new DropForeignKeyConstraintStatement(TestContext.ALT_SCHEMA, TABLE_NAME, ALT_SCHEMA_NAME)) {

                    protected boolean expectedException(Database database, JDBCException exception) {                        
                        return !database.supportsSchemas();
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        //fk constraint is not stored in the alt schema, how can we best test it?
//                        assertNotNull(snapshot.getForeignKey(ALT_SCHEMA_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        //fk constraint is not stored in the alt schema, how can we best test it?
//                        assertNull(snapshot.getForeignKey(ALT_SCHEMA_NAME));
                    }

                });
    }
}
