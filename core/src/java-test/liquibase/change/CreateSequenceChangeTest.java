package liquibase.change;

import liquibase.database.OracleDatabase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Tests for {@link CreateSequenceChange}
 */
public class CreateSequenceChangeTest extends AbstractChangeTest {

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Create Sequence", new CreateSequenceChange().getChangeName());
    }

    @Test
    public void generateStatement() throws Exception {
        CreateSequenceChange change = new CreateSequenceChange();
        change.setSequenceName("SEQ_NAME");
        OracleDatabase oracleDatabase = new OracleDatabase();

        change.setMinValue(100);
        assertEquals("CREATE SEQUENCE SEQ_NAME MINVALUE 100", change.generateStatements(oracleDatabase)[0]);

        change.setMinValue(null);
        change.setMaxValue(1000);
        assertEquals("CREATE SEQUENCE SEQ_NAME MAXVALUE 1000", change.generateStatements(oracleDatabase)[0]);

        change.setMaxValue(null);
        change.setIncrementBy(50);
        assertEquals("CREATE SEQUENCE SEQ_NAME INCREMENT BY 50", change.generateStatements(oracleDatabase)[0]);

        change.setIncrementBy(null);
        change.setOrdered(true);
        assertEquals("CREATE SEQUENCE SEQ_NAME ORDER", change.generateStatements(oracleDatabase)[0]);

        change.setMinValue(1);
        change.setMaxValue(2);
        change.setIncrementBy(3);
        change.setStartValue(4);
        assertEquals("CREATE SEQUENCE SEQ_NAME START WITH 4 INCREMENT BY 3 MINVALUE 1 MAXVALUE 2 ORDER", change.generateStatements(oracleDatabase)[0]);

    }

    @Test
    public void getConfirmationMessage() throws Exception {
        CreateSequenceChange change = new CreateSequenceChange();
        change.setSequenceName("SEQ_NAME");

        assertEquals("Sequence SEQ_NAME has been created", change.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        CreateSequenceChange change = new CreateSequenceChange();
        change.setSequenceName("SEQ_NAME");

        Element node = change.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("createSequence", node.getNodeName());
        assertEquals("SEQ_NAME", node.getAttribute("sequenceName"));
        assertFalse(node.hasAttribute("incrementBy"));
        assertFalse(node.hasAttribute("maxValue"));
        assertFalse(node.hasAttribute("minValue"));
        assertFalse(node.hasAttribute("ordered"));
        assertFalse(node.hasAttribute("startValue"));

        change.setIncrementBy(1);
        change.setMaxValue(2);
        change.setMinValue(3);
        change.setOrdered(true);
        change.setStartValue(4);

        node = change.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("createSequence", node.getNodeName());
        assertEquals("SEQ_NAME", node.getAttribute("sequenceName"));
        assertEquals("1", node.getAttribute("incrementBy"));
        assertEquals("2", node.getAttribute("maxValue"));
        assertEquals("3", node.getAttribute("minValue"));
        assertEquals("true", node.getAttribute("ordered"));
        assertEquals("4", node.getAttribute("startValue"));
    }
}