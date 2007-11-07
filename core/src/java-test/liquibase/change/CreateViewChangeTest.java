package liquibase.change;

import liquibase.database.Database;
import liquibase.database.sql.CreateViewStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import static org.junit.Assert.*;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

public class CreateViewChangeTest extends AbstractChangeTest {


    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Create View", new CreateViewChange().getChangeName());
    }

    @Test
    public void generateStatement() throws Exception {

        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                CreateViewChange change = new CreateViewChange();
                change.setSchemaName("SCHEMA_NAME");
                change.setViewName("VIEW_NAME");
                change.setSelectQuery("SELECT * FROM EXISTING_TABLE");

                SqlStatement[] sqlStatements = change.generateStatements(database);
                assertEquals(1, sqlStatements.length);
                assertTrue(sqlStatements[0] instanceof CreateViewStatement);

                assertEquals("SCHEMA_NAME", ((CreateViewStatement) sqlStatements[0]).getSchemaName());
                assertEquals("VIEW_NAME", ((CreateViewStatement) sqlStatements[0]).getViewName());
                assertEquals("SELECT * FROM EXISTING_TABLE", ((CreateViewStatement) sqlStatements[0]).getSelectQuery());
            }
        });
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        CreateViewChange change = new CreateViewChange();
        change.setViewName("VIEW_NAME");

        assertEquals("View VIEW_NAME created", change.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        CreateViewChange change = new CreateViewChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setViewName("VIEW_NAME");
        change.setSelectQuery("SELECT * FROM EXISTING_TABLE");

        Element node = change.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("createView", node.getTagName());
        assertEquals("SCHEMA_NAME", node.getAttribute("schemaName"));
        assertEquals("VIEW_NAME", node.getAttribute("viewName"));
        assertEquals("SELECT * FROM EXISTING_TABLE", node.getTextContent());
    }
}
