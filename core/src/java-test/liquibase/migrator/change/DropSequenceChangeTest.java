package liquibase.migrator.change;

import static org.junit.Assert.assertEquals;

import javax.xml.parsers.DocumentBuilderFactory;

import liquibase.database.OracleDatabase;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * Tests for {@link DropSequenceChange}
 */
public class DropSequenceChangeTest extends AbstractChangeTest {

    private DropSequenceChange change;

    @Before
    public void setUp() throws Exception {
        change = new DropSequenceChange();
        change.setSequenceName("SEQ_NAME");
    }

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Drop Sequence", new DropSequenceChange().getChangeName());
    }

    @Test
    public void generateStatement() throws Exception {
        assertEquals("DROP SEQUENCE SEQ_NAME", change.generateStatements(new OracleDatabase())[0]);
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        assertEquals("Sequence SEQ_NAME dropped", change.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        Element element = change.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());

        assertEquals("dropSequence", element.getTagName());
        assertEquals("SEQ_NAME", element.getAttribute("sequenceName"));
    }
}