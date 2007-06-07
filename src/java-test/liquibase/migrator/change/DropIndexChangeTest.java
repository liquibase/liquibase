package liquibase.migrator.change;

import liquibase.database.OracleDatabase;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

public class DropIndexChangeTest extends AbstractChangeTest {
    public void testGetRefactoringName() throws Exception {
        assertEquals("Drop Index", new DropIndexChange().getChangeName());
    }

    public void testGenerateStatement() throws Exception {
        DropIndexChange refactoring = new DropIndexChange();
        refactoring.setIndexName("IDX_NAME");
        refactoring.setTableName("TABLE_NAME");

        assertEquals("DROP INDEX IDX_NAME", refactoring.generateStatements(new OracleDatabase())[0]);
    }

    public void testGetConfirmationMessage() throws Exception {
        DropIndexChange refactoring = new DropIndexChange();
        refactoring.setIndexName("IDX_NAME");
        refactoring.setTableName("TABLE_NAME");

        assertEquals("Index IDX_NAME dropped from table TABLE_NAME", refactoring.getConfirmationMessage());
    }

    public void testCreateNode() throws Exception {
        DropIndexChange refactoring = new DropIndexChange();
        refactoring.setIndexName("IDX_NAME");
        Element element = refactoring.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());

        assertEquals("dropIndex", element.getTagName());
        assertEquals("IDX_NAME", element.getAttribute("indexName"));
    }
}