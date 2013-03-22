package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.MockDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropPrimaryKeyStatement;
import static org.junit.Assert.*;
import org.junit.Test;

public class DropPrimaryKeyChangeTest extends StandardChangeTest {
        @Override
        @Test
    public void getRefactoringName() throws Exception {
        assertEquals("dropPrimaryKey", new DropPrimaryKeyChange().getChangeMetaData().getName());
    }

    @Override
    @Test
    public void generateStatement() throws Exception {
        DropPrimaryKeyChange change = new DropPrimaryKeyChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setConstraintName("PK_NAME");

        SqlStatement[] sqlStatements = change.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof DropPrimaryKeyStatement);
        assertEquals("SCHEMA_NAME", ((DropPrimaryKeyStatement) sqlStatements[0]).getSchemaName());
        assertEquals("TABLE_NAME", ((DropPrimaryKeyStatement) sqlStatements[0]).getTableName());
        assertEquals("PK_NAME", ((DropPrimaryKeyStatement) sqlStatements[0]).getConstraintName());
    }

    @Override
    @Test
    public void getConfirmationMessage() throws Exception {
        DropPrimaryKeyChange change = new DropPrimaryKeyChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setConstraintName("PK_NAME");
        assertEquals("Primary key dropped from TABLE_NAME", change.getConfirmationMessage());

    }

    @Override
    protected boolean changeIsUnsupported(Database database) {
        return database instanceof SQLiteDatabase;
    }
}
