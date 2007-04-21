package liquibase.migrator.change;

import liquibase.database.OracleDatabase;
import liquibase.database.struture.DatabaseStructure;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Arrays;
import java.util.HashSet;

public class DropIndexChangeTest extends AbstractChangeTest {
    public void testGetRefactoringName() throws Exception {
        assertEquals("Drop Index", new DropIndexChange().getRefactoringName());
    }

    public void testGenerateStatement() throws Exception {
        DropIndexChange refactoring = new DropIndexChange();
        refactoring.setIndexName("IDX_NAME");
        refactoring.setTableName("TABLE_NAME");

        assertEquals("DROP INDEX IDX_NAME", refactoring.generateStatement(new OracleDatabase()));
    }

    public void testGetConfirmationMessage() throws Exception {
        DropIndexChange refactoring = new DropIndexChange();
        refactoring.setIndexName("IDX_NAME");
        refactoring.setTableName("TABLE_NAME");

        assertEquals("Index IDX_NAME dropped from table TABLE_NAME", refactoring.getConfirmationMessage());
    }

    public void testIsApplicableTo() throws Exception {
        DropIndexChange refactoring = new DropIndexChange();
        assertFalse(refactoring.isApplicableTo(new HashSet<DatabaseStructure>(Arrays.asList(new DatabaseStructure[] {
                createTableDatabaseStructure(),
        }))));

        assertTrue(refactoring.isApplicableTo(new HashSet<DatabaseStructure>(Arrays.asList(new DatabaseStructure[] {
                createIndexDatabaseStructure(),
        }))));
        assertFalse(refactoring.isApplicableTo(new HashSet<DatabaseStructure>(Arrays.asList(new DatabaseStructure[] {
                createIndexDatabaseStructure(),
                createIndexDatabaseStructure(),
        }))));

    }

    public void testCreateNode() throws Exception {
        DropIndexChange refactoring = new DropIndexChange();
        refactoring.setIndexName("IDX_NAME");
        Element element = refactoring.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());

        assertEquals("dropIndex", element.getTagName());
        assertEquals("IDX_NAME", element.getAttribute("indexName"));
    }
}