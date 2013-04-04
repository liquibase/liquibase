package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.MockDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.SetNullableStatement;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests for {@link DropNotNullConstraintChange}
 */
public class DropNotNullConstraintChangeTest extends StandardChangeTest {

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("dropNotNullConstraint", new DropNotNullConstraintChange().getChangeMetaData().getName());
    }

    @Override
    @Test
    public void generateStatement() throws Exception {
        DropNotNullConstraintChange change = new DropNotNullConstraintChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");
        change.setColumnDataType("varchar(200)");

        SqlStatement[] sqlStatements = change.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof SetNullableStatement);
        assertEquals("SCHEMA_NAME", ((SetNullableStatement) sqlStatements[0]).getSchemaName());
        assertEquals("TABLE_NAME", ((SetNullableStatement) sqlStatements[0]).getTableName());
        assertEquals("COL_HERE", ((SetNullableStatement) sqlStatements[0]).getColumnName());
        assertEquals("varchar(200)", ((SetNullableStatement) sqlStatements[0]).getColumnDataType());
        assertTrue(((SetNullableStatement) sqlStatements[0]).isNullable());
    }

    @Override
    @Test
    public void getConfirmationMessage() throws Exception {
        DropNotNullConstraintChange change = new DropNotNullConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");
        assertEquals("Null constraint dropped from TABLE_NAME.COL_HERE", change.getConfirmationMessage());

    }

    @Override
    protected boolean changeIsUnsupported(Database database) {
        return database instanceof FirebirdDatabase
                || database instanceof SQLiteDatabase;
    }

}