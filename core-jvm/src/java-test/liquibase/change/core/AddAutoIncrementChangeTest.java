package liquibase.change.core;

import liquibase.database.*;
import liquibase.database.core.*;
import liquibase.statement.*;
import liquibase.statement.core.SetNullableStatement;
import liquibase.statement.core.CreateSequenceStatement;
import liquibase.statement.core.AddAutoIncrementStatement;
import liquibase.statement.core.AddDefaultValueStatement;
import liquibase.change.core.AddAutoIncrementChange;
import liquibase.change.AbstractChangeTest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class AddAutoIncrementChangeTest extends AbstractChangeTest {

    @Test
    public void constructor() {
        AddAutoIncrementChange change = new AddAutoIncrementChange();
        assertEquals("addAutoIncrement", change.getChangeMetaData().getName());
        assertEquals("Set Column as Auto-Increment", change.getChangeMetaData().getDescription());
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
        assertEquals("Set Column as Auto-Increment", new AddAutoIncrementChange().getChangeMetaData().getDescription());
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
                || (database instanceof HsqlDatabase && !(database instanceof H2Database));
    }

}
