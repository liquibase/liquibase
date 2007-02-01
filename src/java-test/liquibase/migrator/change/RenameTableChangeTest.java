package liquibase.migrator.change;

import liquibase.database.OracleDatabase;
import liquibase.database.struture.DatabaseStructure;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Arrays;
import java.util.HashSet;

public class RenameTableChangeTest extends AbstractChangeTest {
    private RenameTableChange refactoring;

    protected void setUp() throws Exception {
        super.setUp();
        refactoring = new RenameTableChange();
    }

    public void testGetRefactoringName() throws Exception {
        assertEquals("Rename Table", refactoring.getRefactoringName());
    }

    public void testGenerateStatement() throws Exception {
        RenameTableChange refactoring = new RenameTableChange();
        refactoring.setOldTableName("OLD_NAME");
        refactoring.setNewTableName("NEW_NAME");

        assertEquals("rename OLD_NAME to NEW_NAME", refactoring.generateStatement(new OracleDatabase()));
    }

    public void testGetConfirmationMessage() throws Exception {
        refactoring.setOldTableName("OLD_NAME");
        refactoring.setNewTableName("NEW_NAME");

        assertEquals("Table with the name OLD_NAME has been renamed to NEW_NAME", refactoring.getConfirmationMessage());
    }

    public void testIsApplicableTo() throws Exception {
        assertTrue(refactoring.isApplicableTo(new HashSet<DatabaseStructure>(Arrays.asList(new DatabaseStructure[] {
                createTableDatabaseStructure(),
        }))));

        assertFalse(refactoring.isApplicableTo(new HashSet<DatabaseStructure>(Arrays.asList(new DatabaseStructure[] {
                createDatabaseSystem(),
        }))));
    }

    public void testCreateNode() throws Exception {
        refactoring.setOldTableName("OLD_NAME");
        refactoring.setNewTableName("NEW_NAME");

        Element node = refactoring.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("OLD_NAME", refactoring.getOldTableName());
        assertEquals("NEW_NAME", refactoring.getNewTableName());
    }
}
