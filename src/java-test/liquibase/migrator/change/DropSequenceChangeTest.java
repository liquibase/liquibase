package liquibase.migrator.change;

import liquibase.database.OracleDatabase;
import liquibase.database.struture.DatabaseStructure;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Arrays;
import java.util.HashSet;

public class DropSequenceChangeTest extends AbstractChangeTest {
    private DropSequenceChange change;

    protected void setUp() throws Exception {
        super.setUp();
        change = new DropSequenceChange();
        change.setSequenceName("SEQ_NAME");
    }

    public void testGetRefactoringName() throws Exception {
        assertEquals("Drop Sequence", new DropSequenceChange().getRefactoringName());
    }

    public void testGenerateStatement() throws Exception {
        assertEquals("DROP SEQUENCE SEQ_NAME", change.generateStatement(new OracleDatabase()));
    }

    public void testGetConfirmationMessage() throws Exception {
        assertEquals("Sequence SEQ_NAME dropped", change.getConfirmationMessage());
    }

    public void testIsApplicableTo() throws Exception {
        assertFalse(change.isApplicableTo(new HashSet<DatabaseStructure>(Arrays.asList(new DatabaseStructure[] {
                createTableDatabaseStructure(),
        }))));

        assertTrue(change.isApplicableTo(new HashSet<DatabaseStructure>(Arrays.asList(new DatabaseStructure[] {
                createSequenceDatabaseStructure(),
        }))));
        assertFalse(change.isApplicableTo(new HashSet<DatabaseStructure>(Arrays.asList(new DatabaseStructure[] {
                createSequenceDatabaseStructure(),
                createSequenceDatabaseStructure(),
        }))));

    }

    public void testCreateNode() throws Exception {
        Element element =change.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());

        assertEquals("dropSequence", element.getTagName());
        assertEquals("SEQ_NAME", element.getAttribute("sequenceName"));
    }

}