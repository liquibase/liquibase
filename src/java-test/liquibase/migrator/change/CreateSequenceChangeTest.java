package liquibase.migrator.change;

import liquibase.database.OracleDatabase;
import liquibase.database.struture.DatabaseStructure;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Arrays;
import java.util.HashSet;

public class CreateSequenceChangeTest extends AbstractChangeTest {
        public void testGetRefactoringName() throws Exception {
        assertEquals("Create Sequence", new CreateSequenceChange().getRefactoringName());
    }

    public void testGenerateStatement() throws Exception {
        CreateSequenceChange change = new CreateSequenceChange();
        change.setSequenceName("SEQ_NAME");
        OracleDatabase oracleDatabase = new OracleDatabase();

        change.setMinValue(100);
        assertEquals("CREATE SEQUENCE SEQ_NAME MINVALUE 100", change.generateStatement(oracleDatabase));

        change.setMinValue(null);
        change.setMaxValue(1000);
        assertEquals("CREATE SEQUENCE SEQ_NAME MAXVALUE 1000", change.generateStatement(oracleDatabase));

        change.setMaxValue(null);
        change.setIncrementBy(50);
        assertEquals("CREATE SEQUENCE SEQ_NAME INCREMENT BY 50", change.generateStatement(oracleDatabase));

        change.setIncrementBy(null);
        change.setOrdered(true);
        assertEquals("CREATE SEQUENCE SEQ_NAME ORDER", change.generateStatement(oracleDatabase));

        change.setMinValue(1);
        change.setMaxValue(2);
        change.setIncrementBy(3);
        change.setStartValue(4);
        assertEquals("CREATE SEQUENCE SEQ_NAME START WITH 4 INCREMENT BY 3 MINVALUE 1 MAXVALUE 2 ORDER", change.generateStatement(oracleDatabase));

    }

    public void testGetConfirmationMessage() throws Exception {
        CreateSequenceChange change = new CreateSequenceChange();
        change.setSequenceName("SEQ_NAME");

        assertEquals("Sequence SEQ_NAME has been created", change.getConfirmationMessage());
    }

    public void testIsApplicableTo() throws Exception {
        CreateSequenceChange change = new CreateSequenceChange();
        assertFalse(change.isApplicableTo(new HashSet<DatabaseStructure>(Arrays.asList(new DatabaseStructure[] {
                createTableDatabaseStructure(),
        }))));

        assertFalse(change.isApplicableTo(new HashSet<DatabaseStructure>(Arrays.asList(new DatabaseStructure[] {
                createSequenceDatabaseStructure(),
        }))));

        assertTrue(change.isApplicableTo(new HashSet<DatabaseStructure>(Arrays.asList(new DatabaseStructure[] {
                createDatabaseSystem(),
        }))));

        assertFalse(change.isApplicableTo(new HashSet<DatabaseStructure>(Arrays.asList(new DatabaseStructure[] {
                createDatabaseSystem(),
                createDatabaseSystem(),
        }))));

    }

    public void testCreateNode() throws Exception {
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