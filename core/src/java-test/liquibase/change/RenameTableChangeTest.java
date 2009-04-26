package liquibase.change;

import liquibase.database.MockDatabase;
import liquibase.database.statement.RenameTableStatement;
import liquibase.database.statement.SqlStatement;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Tests for {@link RenameTableChange}
 */
public class RenameTableChangeTest extends AbstractChangeTest {

    private RenameTableChange refactoring;

    @Before
    public void setUp() throws Exception {
        refactoring = new RenameTableChange();
    }

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Rename Table", refactoring.getDescription());
    }

    @Test
    public void generateStatement() throws Exception {
        RenameTableChange refactoring = new RenameTableChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setOldTableName("OLD_NAME");
        refactoring.setNewTableName("NEW_NAME");


        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof RenameTableStatement);
        assertEquals("SCHEMA_NAME", ((RenameTableStatement) sqlStatements[0]).getSchemaName());
        assertEquals("OLD_NAME", ((RenameTableStatement) sqlStatements[0]).getOldTableName());
        assertEquals("NEW_NAME", ((RenameTableStatement) sqlStatements[0]).getNewTableName());
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        refactoring.setOldTableName("OLD_NAME");
        refactoring.setNewTableName("NEW_NAME");

        assertEquals("Table OLD_NAME renamed to NEW_NAME", refactoring.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setOldTableName("OLD_NAME");
        refactoring.setNewTableName("NEW_NAME");

        Element node = refactoring.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("renameTable", node.getTagName());
        assertEquals("SCHEMA_NAME", node.getAttribute("schemaName"));
        assertEquals("OLD_NAME", node.getAttribute("oldTableName"));
        assertEquals("NEW_NAME", node.getAttribute("newTableName"));
    }
}
