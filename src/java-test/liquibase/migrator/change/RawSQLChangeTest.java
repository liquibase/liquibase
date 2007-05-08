package liquibase.migrator.change;

import liquibase.database.OracleDatabase;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

public class RawSQLChangeTest extends AbstractChangeTest {
    private RawSQLChange refactoring;

    protected void setUp() throws Exception {
        super.setUp();
        refactoring = new RawSQLChange();
    }

    public void testGetRefactoringName() throws Exception {
        assertEquals("Custom SQL", new RawSQLChange().getRefactoringName());
    }

    public void testGenerateStatement() throws Exception {
        refactoring.setSql("SQL STATEMENT HERE");
        assertEquals("SQL STATEMENT HERE", refactoring.generateStatements(new OracleDatabase())[0]);
    }

    public void testGetConfirmationMessage() throws Exception {
        assertEquals("Custom SQL has been executed", refactoring.getConfirmationMessage());
    }

    public void testCreateNode() throws Exception {
        refactoring.setSql("SOME SQL HERE");

        Element element = refactoring.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("sql", element.getTagName());

        assertEquals("SOME SQL HERE", element.getTextContent());
    }
}