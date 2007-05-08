package liquibase.migrator.change;

import liquibase.database.OracleDatabase;

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

        assertEquals("RENAME OLD_NAME TO NEW_NAME", refactoring.generateStatements(new OracleDatabase())[0]);
    }

    public void testGetConfirmationMessage() throws Exception {
        refactoring.setOldTableName("OLD_NAME");
        refactoring.setNewTableName("NEW_NAME");

        assertEquals("Table with the name OLD_NAME has been renamed to NEW_NAME", refactoring.getConfirmationMessage());
    }

    public void testCreateNode() throws Exception {
        refactoring.setOldTableName("OLD_NAME");
        refactoring.setNewTableName("NEW_NAME");

//        Element node = refactoring.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("OLD_NAME", refactoring.getOldTableName());
        assertEquals("NEW_NAME", refactoring.getNewTableName());
    }
}
