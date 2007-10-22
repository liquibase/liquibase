package liquibase.change;

import liquibase.database.Database;
import liquibase.database.MockDatabase;
import liquibase.database.sql.AddDefaultValueStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.util.ISODateFormat;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Date;

public class AddDefaultValueChangeTest {
    @Test
    public void generateStatements_stringDefaultValue() throws Exception {
        AddDefaultValueChange change = new AddDefaultValueChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COLUMN_NAME");
        change.setDefaultValue("New default value");

        SqlStatement[] statements = change.generateStatements(new MockDatabase());
        assertEquals(1, statements.length);
        AddDefaultValueStatement statement = (AddDefaultValueStatement) statements[0];


        assertEquals("TABLE_NAME", statement.getTableName());
        assertEquals("COLUMN_NAME", statement.getColumnName());
        assertEquals("New default value", statement.getDefaultValue());
    }

    @Test
    public void generateStatements_intDefaultValue() throws Exception {
        AddDefaultValueChange change = new AddDefaultValueChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COLUMN_NAME");
        change.setDefaultValueNumeric("42");

        SqlStatement[] statements = change.generateStatements(new MockDatabase());
        assertEquals(1, statements.length);
        AddDefaultValueStatement statement = (AddDefaultValueStatement) statements[0];


        assertEquals("TABLE_NAME", statement.getTableName());
        assertEquals("COLUMN_NAME", statement.getColumnName());
        assertTrue(statement.getDefaultValue() instanceof Number);
        assertEquals("42", statement.getDefaultValue().toString());
    }

    @Test
    public void generateStatements_decimalDefaultValue() throws Exception {
        AddDefaultValueChange change = new AddDefaultValueChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COLUMN_NAME");
        change.setDefaultValueNumeric("42.56");

        SqlStatement[] statements = change.generateStatements(new MockDatabase());
        assertEquals(1, statements.length);
        AddDefaultValueStatement statement = (AddDefaultValueStatement) statements[0];


        assertEquals("TABLE_NAME", statement.getTableName());
        assertEquals("COLUMN_NAME", statement.getColumnName());
        assertTrue(statement.getDefaultValue() instanceof Number);
        assertEquals("42.56", statement.getDefaultValue().toString());
    }

    @Test
    public void generateStatements_booleanDefaultValue_true() throws Exception {
        AddDefaultValueChange change = new AddDefaultValueChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COLUMN_NAME");
        change.setDefaultValueBoolean(Boolean.TRUE);

        SqlStatement[] statements = change.generateStatements(new MockDatabase());
        assertEquals(1, statements.length);
        AddDefaultValueStatement statement = (AddDefaultValueStatement) statements[0];


        assertEquals("TABLE_NAME", statement.getTableName());
        assertEquals("COLUMN_NAME", statement.getColumnName());
        assertTrue(statement.getDefaultValue() instanceof Boolean);
        assertEquals(Boolean.TRUE, statement.getDefaultValue());
    }

    @Test
    public void generateStatements_booleanDefaultValue_false() throws Exception {
        AddDefaultValueChange change = new AddDefaultValueChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COLUMN_NAME");
        change.setDefaultValueBoolean(Boolean.FALSE);

        SqlStatement[] statements = change.generateStatements(new MockDatabase());
        assertEquals(1, statements.length);
        AddDefaultValueStatement statement = (AddDefaultValueStatement) statements[0];


        assertEquals("TABLE_NAME", statement.getTableName());
        assertEquals("COLUMN_NAME", statement.getColumnName());
        assertTrue(statement.getDefaultValue() instanceof Boolean);
        assertEquals(Boolean.FALSE, statement.getDefaultValue());
    }

    @Test
    public void generateStatements_dateDefaultValue() throws Exception {
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                java.sql.Date date = new java.sql.Date(new Date().getTime());

                AddDefaultValueChange change = new AddDefaultValueChange();
                change.setTableName("TABLE_NAME");
                change.setColumnName("COLUMN_NAME");
                ISODateFormat dateFormat = new ISODateFormat();
                change.setDefaultValueDate(dateFormat.format(date));

                SqlStatement[] statements = change.generateStatements(new MockDatabase());
                assertEquals(1, statements.length);
                AddDefaultValueStatement statement = (AddDefaultValueStatement) statements[0];


                assertEquals("TABLE_NAME", statement.getTableName());
                assertEquals("COLUMN_NAME", statement.getColumnName());
                assertTrue(statement.getDefaultValue() instanceof java.sql.Date);
                assertEquals(date.toString(), statement.getDefaultValue().toString());
            }
        });
    }
}
