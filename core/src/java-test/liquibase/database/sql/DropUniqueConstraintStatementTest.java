package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import org.junit.Test;

public class DropUniqueConstraintStatementTest  extends AbstractSqlStatementTest {
    private static final String TABLE_NAME = "DropUQConstTest";
    private static final String COL_NAME = "colName";
    private static final String CONSTRAINT_NAME = "UQ_dropUQ";
    private static final String UNIQUE_COLUMNS = COL_NAME;

    protected void setupDatabase(Database database) throws Exception {
        dropAndCreateTable(new CreateTableStatement(null, TABLE_NAME)
                .addPrimaryKeyColumn("id", "int", null, null)
                .addColumn(COL_NAME, "varchar(50)", new NotNullConstraint(), new UniqueConstraint(CONSTRAINT_NAME))
                , database);
    }

    protected SqlStatement generateTestStatement() {
        return new DropUniqueConstraintStatement(null, null, null);
    }

    @Test
    public void execute_defaultSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new DropUniqueConstraintStatement(null, TABLE_NAME, CONSTRAINT_NAME, UNIQUE_COLUMNS)) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        //todo: assert when isUnique works: assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COL_NAME).isUnique());
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        //todo: assert when isUnique works: assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COL_NAME).isUnique());
                    }

                });
    }

}
