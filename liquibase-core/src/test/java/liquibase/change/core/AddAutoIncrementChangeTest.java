package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import liquibase.change.ChangeMetaData;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddAutoIncrementStatement;
import liquibase.statement.core.AddDefaultValueStatement;
import liquibase.statement.core.CreateSequenceStatement;
import liquibase.statement.core.SetNullableStatement;
import static org.junit.Assert.*;
import org.junit.Test;

public class AddAutoIncrementChangeTest extends StandardChangeTest {

    @Test
    public void constructor() {
        AddAutoIncrementChange change = new AddAutoIncrementChange();
        assertEquals("column", change.getChangeMetaData().getAppliesTo().iterator().next());
    }

    @Override
    @Test
    public void generateStatement() throws Exception {
        AddAutoIncrementChange change = new AddAutoIncrementChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnName("COLUMN_NAME");
        change.setColumnDataType("DATATYPE(255)");

        testChangeOnAllExcept(change, new GenerateAllValidator() {
            public void validate(SqlStatement[] sqlStatements, Database database) {

                assertEquals(1, sqlStatements.length);
                assertTrue(sqlStatements[0] instanceof AddAutoIncrementStatement);
                assertEquals("SCHEMA_NAME", ((AddAutoIncrementStatement) sqlStatements[0]).getSchemaName());
                assertEquals("TABLE_NAME", ((AddAutoIncrementStatement) sqlStatements[0]).getTableName());
                assertEquals("COLUMN_NAME", ((AddAutoIncrementStatement) sqlStatements[0]).getColumnName());
                assertEquals("DATATYPE(255)", ((AddAutoIncrementStatement) sqlStatements[0]).getColumnDataType());
            }
        }, PostgresDatabase.class);
        testChange(change, new GenerateAllValidator() {
            public void validate(SqlStatement[] sqlStatements, Database database) {

                assertEquals(3, sqlStatements.length);
                assertTrue(sqlStatements[0] instanceof CreateSequenceStatement);
                assertTrue(sqlStatements[1] instanceof SetNullableStatement);
                assertTrue(sqlStatements[2] instanceof AddDefaultValueStatement);
            }
        }, PostgresDatabase.class);
    }

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("addAutoIncrement", new AddAutoIncrementChange().getChangeMetaData().getName());
    }

    @Override
    @Test
    public void getConfirmationMessage() throws Exception {
        AddAutoIncrementChange change = new AddAutoIncrementChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnName("COLUMN_NAME");
        change.setColumnDataType("DATATYPE(255)");

        assertEquals("Auto-increment added to TABLE_NAME.COLUMN_NAME", change.getConfirmationMessage());
    }

    @Override
    protected boolean changeIsUnsupported(Database database) {
        return !database.supportsAutoIncrement()
                || database instanceof MSSQLDatabase
                || database instanceof DerbyDatabase
                ;//|| (database instanceof HsqlDatabase);
    }

    @Test
    public void changeMetaDataCreatedCorrectly() {
        AddAutoIncrementChange change = new AddAutoIncrementChange();
        ChangeMetaData metaData = change.getChangeMetaData();
        assertEquals("addAutoIncrement", metaData.getName());

    }

}
