package liquibase.change;

import liquibase.database.Database;
import liquibase.database.statement.CreateIndexStatement;
import liquibase.database.statement.SqlStatement;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import static org.junit.Assert.*;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Tests for {@link CreateIndexChange}
 */
public class CreateIndexChangeTest extends AbstractChangeTest {

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Create Index", new CreateIndexChange().getDescription());
    }

    @Test
    public void generateStatement() throws Exception {
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                CreateIndexChange change = new CreateIndexChange();
                change.setIndexName("IDX_NAME");
                change.setSchemaName("SCHEMA_NAME");
                change.setTableName("TABLE_NAME");
                change.setTablespace("TABLESPACE_NAME");

                ColumnConfig column = new ColumnConfig();
                column.setName("COL_NAME");
                change.addColumn(column);

                ColumnConfig column2 = new ColumnConfig();
                column2.setName("COL2_NAME");
                change.addColumn(column2);

                SqlStatement[] sqlStatements = change.generateStatements(database);
                assertEquals(1, sqlStatements.length);
                assertTrue(sqlStatements[0] instanceof CreateIndexStatement);

                assertEquals("IDX_NAME", ((CreateIndexStatement) sqlStatements[0]).getIndexName());
                assertEquals("SCHEMA_NAME", ((CreateIndexStatement) sqlStatements[0]).getTableSchemaName());
                assertEquals("TABLE_NAME", ((CreateIndexStatement) sqlStatements[0]).getTableName());
                assertEquals("TABLESPACE_NAME", ((CreateIndexStatement) sqlStatements[0]).getTablespace());
                assertEquals(2, ((CreateIndexStatement) sqlStatements[0]).getColumns().length);
                assertEquals("COL_NAME", ((CreateIndexStatement) sqlStatements[0]).getColumns()[0]);
                assertEquals("COL2_NAME", ((CreateIndexStatement) sqlStatements[0]).getColumns()[1]);
            }
        });
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        CreateIndexChange refactoring = new CreateIndexChange();
        refactoring.setIndexName("IDX_TEST");

        assertEquals("Index IDX_TEST created", refactoring.getConfirmationMessage());
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