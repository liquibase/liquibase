package liquibase.change.core;

import liquibase.change.AbstractChangeTest;
import liquibase.change.ColumnConfig;
import liquibase.database.core.MockDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertStatement;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link InsertDataChange}
 */
public class InsertDataChangeTest extends AbstractChangeTest {

    InsertDataChange refactoring;

    @Before
    public void setUp() throws Exception {
        refactoring = new InsertDataChange();
        refactoring.setTableName("TABLE_NAME");

        ColumnConfig col1 = new ColumnConfig();
        col1.setName("id");
        col1.setValueNumeric("123");

        ColumnConfig col2 = new ColumnConfig();
        col2.setName("name");
        col2.setValue("Andrew");

        ColumnConfig col3 = new ColumnConfig();
        col3.setName("age");
        col3.setValueNumeric("21");
        
        ColumnConfig col4 = new ColumnConfig();
        col4.setName("height");
        col4.setValueNumeric("1.78");

        refactoring.addColumn(col1);
        refactoring.addColumn(col2);
        refactoring.addColumn(col3);
        refactoring.addColumn(col4);
    }

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Insert Row", refactoring.getChangeMetaData().getDescription());
    }

    @Override
    @Test
    public void generateStatement() throws Exception {
        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof InsertStatement);
        assertEquals("123", ((InsertStatement) sqlStatements[0]).getColumnValue("id").toString());
        assertEquals("Andrew", ((InsertStatement) sqlStatements[0]).getColumnValue("name").toString());
        assertEquals("21", ((InsertStatement) sqlStatements[0]).getColumnValue("age").toString());
        assertEquals("1.78", ((InsertStatement) sqlStatements[0]).getColumnValue("height").toString());
    }

    @Override
    @Test
    public void getConfirmationMessage() throws Exception {
        assertEquals("New row inserted into TABLE_NAME", refactoring.getConfirmationMessage());
    }
}
