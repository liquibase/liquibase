package liquibase.change;

import liquibase.database.Database;
import liquibase.database.MockDatabase;
import liquibase.database.statement.AddDefaultValueStatement;
import liquibase.database.statement.SqlStatement;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.util.ISODateFormat;
import static org.junit.Assert.*;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Date;

public class AddDefaultValueChangeTest extends AbstractChangeTest {

    @Test
    public void generateStatement() throws Exception {
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

    public void getRefactoringName() throws Exception {
        assertEquals("Add Default Value", new AddDefaultValueChange().getDescription());
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        AddDefaultValueChange change = new AddDefaultValueChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnName("COLUMN_NAME");

        assertEquals("Default value added to TABLE_NAME.COLUMN_NAME", change.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        AddDefaultValueChange change = new AddDefaultValueChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnName("COLUMN_NAME");
        change.setDefaultValue("DEF STRING");
        change.setDefaultValueNumeric("42");
        change.setDefaultValueBoolean(true);
        change.setDefaultValueDate("2007-01-02");

        Element node = change.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("addDefaultValue", node.getTagName());
        assertEquals("SCHEMA_NAME", node.getAttribute("schemaName"));
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("COLUMN_NAME", node.getAttribute("columnName"));
        assertEquals("DEF STRING", node.getAttribute("defaultValue"));
        assertEquals("42", node.getAttribute("defaultValueNumeric"));
        assertEquals("true", node.getAttribute("defaultValueBoolean"));
        assertEquals("2007-01-02", node.getAttribute("defaultValueDate"));

    }

    @Test
    public void getMD5Sum() throws Exception {
        AddDefaultValueChange change = new AddDefaultValueChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnName("COLUMN_NAME");
        change.setDefaultValue("DEF STRING");
        change.setDefaultValueNumeric("42");
        change.setDefaultValueBoolean(true);
        change.setDefaultValueDate("2007-01-02");

        String md5sum1 = change.generateCheckSum();

        change.setSchemaName("SCHEMA_NAME2");
        String md5Sum2 = change.generateCheckSum();

        assertFalse(md5sum1.equals(md5Sum2));

        change.setSchemaName("SCHEMA_NAME");
        String md5Sum3 = change.generateCheckSum();

        assertTrue(md5sum1.equals(md5Sum3));

    }


}
