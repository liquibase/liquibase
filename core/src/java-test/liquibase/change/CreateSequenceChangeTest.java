package liquibase.change;

import liquibase.database.Database;
import liquibase.database.sql.CreateSequenceStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import static org.junit.Assert.*;
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
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                if (!database.supportsSequences()) {
                    return;
                }

                CreateSequenceChange change = new CreateSequenceChange();
                change.setSchemaName("SCHEMA_NAME");
                change.setSequenceName("SEQ_NAME");
                change.setIncrementBy(1);
                change.setMinValue(2);
                change.setMaxValue(3);
                change.setOrdered(true);
                change.setStartValue(4);

                SqlStatement[] sqlStatements = change.generateStatements(database);
                assertEquals(1, sqlStatements.length);
                assertTrue(sqlStatements[0] instanceof CreateSequenceStatement);

                assertEquals("SCHEMA_NAME", ((CreateSequenceStatement) sqlStatements[0]).getSchemaName());
                assertEquals("SEQ_NAME", ((CreateSequenceStatement) sqlStatements[0]).getSequenceName());
                assertEquals(new Integer(1), ((CreateSequenceStatement) sqlStatements[0]).getIncrementBy());
                assertEquals(new Integer(2), ((CreateSequenceStatement) sqlStatements[0]).getMinValue());
                assertEquals(new Integer(3), ((CreateSequenceStatement) sqlStatements[0]).getMaxValue());
                assertEquals(new Integer(4), ((CreateSequenceStatement) sqlStatements[0]).getStartValue());
                assertEquals(true, ((CreateSequenceStatement) sqlStatements[0]).getOrdered());
            }
        });
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        CreateSequenceChange change = new CreateSequenceChange();
        change.setSequenceName("SEQ_NAME");

        assertEquals("Sequence SEQ_NAME created", change.getConfirmationMessage());
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
