package liquibase.change.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import liquibase.change.ChangeFactory;
import liquibase.change.ColumnConfig;
import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.MockDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AlterTableStatement;
import liquibase.statement.core.DropColumnStatement;

import org.junit.Test;

/**
 * Tests for {@link DropColumnChange}
 */
public class DropColumnChangeTest extends StandardChangeTest {

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("dropColumn", ChangeFactory.getInstance().getChangeMetaData(new DropColumnChange()).getName());
    }

    @Override
    @Test
    public void generateStatement() throws Exception {
        DropColumnChange change = new DropColumnChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");

        SqlStatement[] sqlStatements = change.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof DropColumnStatement);
        assertEquals("SCHEMA_NAME", ((DropColumnStatement) sqlStatements[0]).getSchemaName());
        assertEquals("TABLE_NAME", ((DropColumnStatement) sqlStatements[0]).getTableName());
        assertEquals("COL_HERE", ((DropColumnStatement) sqlStatements[0]).getColumnName());
    }

    @Override
    @Test
    public void getConfirmationMessage() throws Exception {
        DropColumnChange change = new DropColumnChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");

        assertEquals("Column TABLE_NAME.COL_HERE dropped", change.getConfirmationMessage());
    }

    @Test
    public void confirmationMessageForTwoColumns() throws Exception {
        DropColumnChange change = createDropTwoColumnsChange();
        assertEquals("Columns column1_name,column2_name dropped from table_name", change.getConfirmationMessage());
    }

    @Test
    public void generateForTwoColumns() throws Exception {
        DropColumnChange change = createDropTwoColumnsChange();

        testChangeOnAll(change, new GenerateAllValidator() {
            @Override
            public void validate(SqlStatement[] sqlStatements, Database database) {
                if (database instanceof MySQLDatabase) {
                    assertEquals(1, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AlterTableStatement);
                    AlterTableStatement statement = (AlterTableStatement) sqlStatements[0];
                    assertEquals(2, statement.getDropColumns().size());
                    assertEquals("column1_name", statement.getDropColumns().get(0).getColumnName());
                    assertEquals("column2_name", statement.getDropColumns().get(1).getColumnName());
                } else if (database instanceof DB2Database) {
                    assertEquals(4, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof DropColumnStatement);
                    assertTrue(sqlStatements[2] instanceof DropColumnStatement);
                    assertEquals("column1_name", ((DropColumnStatement) sqlStatements[0]).getColumnName());
                    assertEquals("column2_name", ((DropColumnStatement) sqlStatements[2]).getColumnName());
                } else {
                    assertEquals(2, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof DropColumnStatement);
                    assertTrue(sqlStatements[1] instanceof DropColumnStatement);
                    assertEquals("column1_name", ((DropColumnStatement) sqlStatements[0]).getColumnName());
                    assertEquals("column2_name", ((DropColumnStatement) sqlStatements[1]).getColumnName());
                }
            }
        });
    }

    private DropColumnChange createDropTwoColumnsChange() {
        DropColumnChange change = new DropColumnChange();
        change.setTableName("table_name");
        ColumnConfig column = new ColumnConfig();
        column.setName("column1_name");
        change.addColumn(column);
        column = new ColumnConfig();
        column.setName("column2_name");
        change.addColumn(column);
        return change;
    }
}