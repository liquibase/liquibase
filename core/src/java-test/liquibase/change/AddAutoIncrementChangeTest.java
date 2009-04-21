package liquibase.change;

import liquibase.database.Database;
import liquibase.database.PostgresDatabase;
import liquibase.database.statement.*;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import static org.junit.Assert.*;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

public class AddAutoIncrementChangeTest extends AbstractChangeTest {

    @Test
    public void constructor() {
        AddAutoIncrementChange change = new AddAutoIncrementChange();
        assertEquals("addAutoIncrement", change.getTagName());
        assertEquals("Set Column as Auto-Increment", change.getChangeName());
    }

    @Test
    public void generateStatement() throws Exception {
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                AddAutoIncrementChange change = new AddAutoIncrementChange();
                change.setSchemaName("SCHEMA_NAME");
                change.setTableName("TABLE_NAME");
                change.setColumnName("COLUMN_NAME");
                change.setColumnDataType("DATATYPE(255)");

                SqlStatement[] sqlStatements = change.generateStatements(database);
                if (database instanceof PostgresDatabase) {
                    assertEquals(3, sqlStatements.length);
                    //todo: improve test as statements are no longer raw statements
                    assertTrue(sqlStatements[0] instanceof CreateSequenceStatement);
                    assertTrue(sqlStatements[1] instanceof SetNullableStatement);
                    assertTrue(sqlStatements[2] instanceof AddDefaultValueStatement);
                } else {
                    assertEquals(1, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddAutoIncrementStatement);
                    assertEquals("SCHEMA_NAME", ((AddAutoIncrementStatement) sqlStatements[0]).getSchemaName());
                    assertEquals("TABLE_NAME", ((AddAutoIncrementStatement) sqlStatements[0]).getTableName());
                    assertEquals("COLUMN_NAME", ((AddAutoIncrementStatement) sqlStatements[0]).getColumnName());
                    assertEquals("DATATYPE(255)", ((AddAutoIncrementStatement) sqlStatements[0]).getColumnDataType());
                }
            }
        });
    }

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Set Column as Auto-Increment", new AddAutoIncrementChange().getChangeName());
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        AddAutoIncrementChange change = new AddAutoIncrementChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnName("COLUMN_NAME");
        change.setColumnDataType("DATATYPE(255)");

        assertEquals("Auto-increment added to TABLE_NAME.COLUMN_NAME", change.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        AddAutoIncrementChange change = new AddAutoIncrementChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnName("COLUMN_NAME");
        change.setColumnDataType("DATATYPE(255)");

        Element node = change.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("addAutoIncrement", node.getTagName());
        assertEquals("SCHEMA_NAME", node.getAttribute("schemaName"));
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("COLUMN_NAME", node.getAttribute("columnName"));
        assertEquals("DATATYPE(255)", node.getAttribute("columnDataType"));
    }
}
