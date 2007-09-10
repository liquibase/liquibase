package liquibase.change;

import liquibase.database.OracleDatabase;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Tests for {@link CreateIndexChange}
 */
public class CreateIndexChangeTest extends AbstractChangeTest {

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Create Index", new CreateIndexChange().getChangeName());
    }

    @Test
    public void generateStatement() throws Exception {
        CreateIndexChange refactoring = new CreateIndexChange();
        refactoring.setIndexName("IDX_TEST");
        refactoring.setTableName("TAB_NAME");

        ColumnConfig column1 = new ColumnConfig();
        column1.setName("COL1");
        refactoring.addColumn(column1);

        OracleDatabase database = new OracleDatabase();
        assertEquals("CREATE INDEX IDX_TEST ON TAB_NAME(COL1)", refactoring.generateStatements(database)[0].getSqlStatement(database));

        ColumnConfig column2 = new ColumnConfig();
        column2.setName("COL2");
        refactoring.addColumn(column2);

        assertEquals("CREATE INDEX IDX_TEST ON TAB_NAME(COL1, COL2)", refactoring.generateStatements(database)[0].getSqlStatement(database));
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        CreateIndexChange refactoring = new CreateIndexChange();
        refactoring.setIndexName("IDX_TEST");

        assertEquals("Index IDX_TEST has been created", refactoring.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        CreateIndexChange refactoring = new CreateIndexChange();
        refactoring.setIndexName("IDX_TEST");
        refactoring.setTableName("TAB_NAME");

        ColumnConfig column1 = new ColumnConfig();
        column1.setName("COL1");
        refactoring.addColumn(column1);

        ColumnConfig column2 = new ColumnConfig();
        column2.setName("COL2");
        refactoring.addColumn(column2);

        Element element = refactoring.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("createIndex", element.getTagName());
        assertEquals("IDX_TEST", element.getAttribute("indexName"));
        assertEquals("TAB_NAME", element.getAttribute("tableName"));

        assertEquals(2, element.getChildNodes().getLength());
        assertEquals("column", ((Element) element.getChildNodes().item(0)).getTagName());
        assertEquals("COL1", ((Element) element.getChildNodes().item(0)).getAttribute("name"));
        assertEquals("column", ((Element) element.getChildNodes().item(1)).getTagName());
        assertEquals("COL2", ((Element) element.getChildNodes().item(1)).getAttribute("name"));
    }
}