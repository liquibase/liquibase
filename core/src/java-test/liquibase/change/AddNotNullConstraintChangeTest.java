package liquibase.change;

import liquibase.database.*;
import liquibase.statement.SetNullableStatement;
import liquibase.statement.SqlStatement;
import liquibase.statement.UpdateStatement;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests for {@link AddNotNullConstraintChange}
 */
public class AddNotNullConstraintChangeTest extends AbstractChangeTest {

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Add Not-Null Constraint", new AddNotNullConstraintChange().getChangeMetaData().getDescription());
    }

    @Test
    public void generateStatement() throws Exception {

        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                AddNotNullConstraintChange change = new AddNotNullConstraintChange();
                change.setSchemaName("SCHEMA_NAME");
                change.setTableName("TABLE_NAME");
                change.setColumnName("COL_HERE");
                change.setDefaultNullValue("DEFAULT_VALUE");
                change.setColumnDataType("varchar(200)");

                SqlStatement[] sqlStatements = change.generateStatements(new MockDatabase());
                assertEquals(2, sqlStatements.length);
                assertTrue(sqlStatements[0] instanceof UpdateStatement);
                assertTrue(sqlStatements[1] instanceof SetNullableStatement);

                assertEquals("SCHEMA_NAME", ((UpdateStatement) sqlStatements[0]).getSchemaName());
                assertEquals("TABLE_NAME", ((UpdateStatement) sqlStatements[0]).getTableName());
                assertEquals("COL_HERE IS NULL", ((UpdateStatement) sqlStatements[0]).getWhereClause());
                assertEquals("DEFAULT_VALUE", ((UpdateStatement) sqlStatements[0]).getNewColumnValues().get("COL_HERE"));

                assertEquals("SCHEMA_NAME", ((SetNullableStatement) sqlStatements[1]).getSchemaName());
                assertEquals("TABLE_NAME", ((SetNullableStatement) sqlStatements[1]).getTableName());
                assertEquals("COL_HERE", ((SetNullableStatement) sqlStatements[1]).getColumnName());
                assertEquals("varchar(200)", ((SetNullableStatement) sqlStatements[1]).getColumnDataType());
                assertFalse(((SetNullableStatement) sqlStatements[1]).isNullable());
            }
        });
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        AddNotNullConstraintChange change = new AddNotNullConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");

        assertEquals("Null constraint has been added to TABLE_NAME.COL_HERE", change.getConfirmationMessage());
    }

    @Override
    protected boolean changeIsUnsupported(Database database) {
        return database instanceof FirebirdDatabase
                || database instanceof SQLiteDatabase;
    }
    
}