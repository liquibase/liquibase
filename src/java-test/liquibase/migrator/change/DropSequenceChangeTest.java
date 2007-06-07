package liquibase.migrator.change;

import liquibase.database.OracleDatabase;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

public class DropSequenceChangeTest extends AbstractChangeTest {
    private DropSequenceChange change;

    protected void setUp() throws Exception {
        super.setUp();
        change = new DropSequenceChange();
        change.setSequenceName("SEQ_NAME");
    }

    public void testGetRefactoringName() throws Exception {
        assertEquals("Drop Sequence", new DropSequenceChange().getChangeName());
    }

    public void testGenerateStatement() throws Exception {
        assertEquals("DROP SEQUENCE SEQ_NAME", change.generateStatements(new OracleDatabase())[0]);
    }

    public void testGetConfirmationMessage() throws Exception {
        assertEquals("Sequence SEQ_NAME dropped", change.getConfirmationMessage());
    }

    public void testCreateNode() throws Exception {
        Element element = change.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());

        assertEquals("dropSequence", element.getTagName());
        assertEquals("SEQ_NAME", element.getAttribute("sequenceName"));
    }

}