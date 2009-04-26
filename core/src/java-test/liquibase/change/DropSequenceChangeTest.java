package liquibase.change;

import liquibase.database.Database;
import liquibase.database.statement.DropSequenceStatement;
import liquibase.database.statement.SqlStatement;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

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
        assertEquals("Drop Sequence", new DropSequenceChange().getDescription());
    }

    @Test
    public void generateStatement() throws Exception {

        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                if (!database.supportsSequences()) {
                    return;
                }

                DropSequenceChange change = new DropSequenceChange();
                change.setSchemaName("SCHEMA_NAME");
                change.setSequenceName("SEQ_NAME");

                SqlStatement[] sqlStatements = change.generateStatements(database);
                assertEquals(1, sqlStatements.length);
                assertTrue(sqlStatements[0] instanceof DropSequenceStatement);

                assertEquals("SCHEMA_NAME", ((DropSequenceStatement) sqlStatements[0]).getSchemaName());
                assertEquals("SEQ_NAME", ((DropSequenceStatement) sqlStatements[0]).getSequenceName());

            }
        });
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