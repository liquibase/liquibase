package liquibase.change;

import liquibase.database.MockDatabase;
import liquibase.database.statement.SqlStatement;
import liquibase.database.statement.TagDatabaseStatement;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

public class TagDatabaseChangeTest extends AbstractChangeTest {

    private TagDatabaseChange refactoring;

    @Before
    public void setUp() throws Exception {
        refactoring = new TagDatabaseChange();
    }

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Tag Database", refactoring.getChangeName());
    }

    @Test
    public void generateStatement() throws Exception {
        TagDatabaseChange refactoring = new TagDatabaseChange();
        refactoring.setTag("TAG_NAME");

        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof TagDatabaseStatement);
        assertEquals("TAG_NAME", ((TagDatabaseStatement) sqlStatements[0]).getTag());
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        refactoring.setTag("TAG_NAME");

        assertEquals("Tag 'TAG_NAME' applied to database", refactoring.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        refactoring.setTag("TAG_NAME");

        Element node = refactoring.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("tagDatabase", node.getTagName());
        assertEquals("TAG_NAME", node.getAttribute("tag"));
    }

}