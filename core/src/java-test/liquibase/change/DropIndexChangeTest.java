package liquibase.change;

import liquibase.database.MockDatabase;
import liquibase.database.statement.DropIndexStatement;
import liquibase.database.statement.SqlStatement;
import static org.junit.Assert.*;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Tests for {@link DropIndexChange}
 */
public class DropIndexChangeTest extends AbstractChangeTest {

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Drop Index", new DropIndexChange().getChangeName());
    }

    @Test
    public void generateStatement() throws Exception {
        DropIndexChange refactoring = new DropIndexChange();
        refactoring.setIndexName("IDX_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setSchemaName("SCHEMA_NAME");

        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof DropIndexStatement);
        assertEquals("SCHEMA_NAME", ((DropIndexStatement) sqlStatements[0]).getTableSchemaName());
        assertEquals("TABLE_NAME", ((DropIndexStatement) sqlStatements[0]).getTableName());
        assertEquals("IDX_NAME", ((DropIndexStatement) sqlStatements[0]).getIndexName());
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        DropIndexChange refactoring = new DropIndexChange();
        refactoring.setIndexName("IDX_NAME");
        refactoring.setTableName("TABLE_NAME");

        assertEquals("Index IDX_NAME dropped from table TABLE_NAME", refactoring.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        DropIndexChange refactoring = new DropIndexChange();
        refactoring.setIndexName("IDX_NAME");
        Element element = refactoring.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());

        assertEquals("dropIndex", element.getTagName());
        assertEquals("IDX_NAME", element.getAttribute("indexName"));
    }
}