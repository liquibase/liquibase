package liquibase.change;

import liquibase.database.OracleDatabase;
import liquibase.util.XMLUtil;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Tests for {@link RawSQLChange}
 */
public class RawSQLChangeTest extends AbstractChangeTest {

    private RawSQLChange refactoring;

    @Before
    public void setUp() throws Exception {
        refactoring = new RawSQLChange();
    }

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Custom SQL", new RawSQLChange().getChangeName());
    }

    @Test
    public void generateStatement() throws Exception {
        refactoring.setSql("SQL STATEMENT HERE");
        OracleDatabase database = new OracleDatabase();
        assertEquals("SQL STATEMENT HERE", refactoring.generateStatements(database)[0].getSqlStatement(database));
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        assertEquals("Custom SQL has been executed", refactoring.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        refactoring.setSql("SOME SQL HERE");

        Element element = refactoring.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("sql", element.getTagName());

        assertEquals("SOME SQL HERE", XMLUtil.getTextContent(element));
    }
}