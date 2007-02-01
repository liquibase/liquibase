package liquibase.migrator.change;

import liquibase.database.OracleDatabase;
import liquibase.database.struture.DatabaseStructure;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Arrays;
import java.util.HashSet;

public class CreateTableChangeTest extends AbstractChangeTest {
    private CreateTableChange change;

    protected void setUp() throws Exception {
        super.setUp();
        change = new CreateTableChange();
    }

    public void testGetRefactoringName() throws Exception {
        assertEquals("Create Table", change.getRefactoringName());
    }

    public void testGenerateStatement() throws Exception {
        change.setTableName("TABLE_NAME");

        ColumnConfig column1 = new ColumnConfig();
        column1.setName("id");
        column1.setType("int");
        ConstraintsConfig column1constraints = new ConstraintsConfig();
        column1constraints.setPrimaryKey(true);
        column1constraints.setNullable(false);
        column1.setConstraints(column1constraints);
        change.addColumn(column1);

        ColumnConfig column2 = new ColumnConfig();
        column2.setName("name");
        column2.setType("varchar(255)");
        change.addColumn(column2);

        ColumnConfig column3 = new ColumnConfig();
        column3.setName("state_id");
        ConstraintsConfig column3constraints = new ConstraintsConfig();
        column3constraints.setNullable(false);
        column3constraints.setInitiallyDeferred(true);
        column3constraints.setDeferrable(true);
        column3constraints.setForeignKeyName("fk_tab_ref");
        column3constraints.setReferences("state(id)");
        column3.setConstraints(column3constraints);
        change.addColumn(column3);

        ColumnConfig column4 = new ColumnConfig();
        column4.setName("phone");
        column4.setType("varchar(255)");
        column4.setDefaultValue("NOPHONE");
        change.addColumn(column4);

        ColumnConfig column5 = new ColumnConfig();
        column5.setName("phone2");
        column5.setType("varchar(255)");
        ConstraintsConfig column5constraints = new ConstraintsConfig();
        column5constraints.setUnique(true);
        column5.setConstraints(column5constraints);
        change.addColumn(column5);

        assertEquals("CREATE TABLE TABLE_NAME (id int NOT NULL PRIMARY KEY, name varchar(255), state_id NOT NULL CONSTRAINT fk_tab_ref REFERENCES state(id) INITIALLY DEFERRED DEFERRABLE, phone varchar(255) DEFAULT 'NOPHONE', phone2 varchar(255) UNIQUE)", change.generateStatement(new OracleDatabase()));
    }

    public void testGetConfirmationMessage() throws Exception {
        change.setTableName("TAB_NAME");
        assertEquals("Table TAB_NAME created", change.getConfirmationMessage());
    }

    public void testIsApplicableTo() throws Exception {
        assertFalse(change.isApplicableTo(new HashSet<DatabaseStructure>(Arrays.asList(new DatabaseStructure[] {
                createTableDatabaseStructure(),
        }))));

        assertTrue(change.isApplicableTo(new HashSet<DatabaseStructure>(Arrays.asList(new DatabaseStructure[] {
                createDatabaseSystem(),
        }))));

    }

    public void testCreateNode() throws Exception {
      change.setTableName("TABLE_NAME");

        ColumnConfig column1 = new ColumnConfig();
        column1.setName("id");
        column1.setType("int");
        ConstraintsConfig column1constraints = new ConstraintsConfig();
        column1constraints.setPrimaryKey(true);
        column1constraints.setNullable(false);
        column1.setConstraints(column1constraints);
        change.addColumn(column1);

        ColumnConfig column2 = new ColumnConfig();
        column2.setName("name");
        column2.setType("varchar(255)");
        change.addColumn(column2);

        ColumnConfig column3 = new ColumnConfig();
        column3.setName("state_id");
        ConstraintsConfig column3constraints = new ConstraintsConfig();
        column3constraints.setNullable(false);
        column3constraints.setInitiallyDeferred(true);
        column3constraints.setDeferrable(true);
        column3constraints.setForeignKeyName("fk_tab_ref");
        column3constraints.setReferences("state(id)");
        column3.setConstraints(column3constraints);
        change.addColumn(column3);

        ColumnConfig column4 = new ColumnConfig();
        column4.setName("phone");
        column4.setType("varchar(255)");
        column4.setDefaultValue("NOPHONE");
        change.addColumn(column4);

        ColumnConfig column5 = new ColumnConfig();
        column5.setName("phone2");
        column5.setType("varchar(255)");
        ConstraintsConfig column5constraints = new ConstraintsConfig();
        column5constraints.setUnique(true);
        column5.setConstraints(column5constraints);
        change.addColumn(column5);

        Element element = change.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("createTable", element.getTagName());
        assertEquals(5, element.getChildNodes().getLength());

        Element columnElement = ((Element) element.getChildNodes().item(0));
        assertEquals("column", columnElement.getTagName());
        assertEquals("id", columnElement.getAttribute("name"));
        assertEquals("int", columnElement.getAttribute("type"));
        Element constraintsElement = (Element) columnElement.getChildNodes().item(0);
        assertEquals("constraints", constraintsElement.getTagName());
        assertEquals(2, constraintsElement.getAttributes().getLength());
        assertEquals("true", constraintsElement.getAttribute("primaryKey"));
        assertEquals("false", constraintsElement.getAttribute("nullable"));

        columnElement = ((Element) element.getChildNodes().item(1));
        assertEquals("column", columnElement.getTagName());
        assertEquals("name", columnElement.getAttribute("name"));
        assertEquals("varchar(255)", columnElement.getAttribute("type"));

        columnElement = ((Element) element.getChildNodes().item(2));
        assertEquals("column", columnElement.getTagName());
        assertEquals("state_id", columnElement.getAttribute("name"));
        constraintsElement = (Element) columnElement.getChildNodes().item(0);
        assertEquals("constraints", constraintsElement.getTagName());
        assertEquals(5, constraintsElement.getAttributes().getLength());
        assertEquals("false", constraintsElement.getAttribute("nullable"));
        assertEquals("true", constraintsElement.getAttribute("deferrable"));
        assertEquals("true", constraintsElement.getAttribute("initiallyDeferred"));
        assertEquals("fk_tab_ref", constraintsElement.getAttribute("foreignKeyName"));
        assertEquals("state(id)", constraintsElement.getAttribute("references"));

        columnElement = ((Element) element.getChildNodes().item(3));
        assertEquals("column", columnElement.getTagName());
        assertEquals("phone", columnElement.getAttribute("name"));
        assertEquals("varchar(255)", columnElement.getAttribute("type"));

        columnElement = ((Element) element.getChildNodes().item(4));
        assertEquals("column", columnElement.getTagName());
        assertEquals("phone2", columnElement.getAttribute("name"));
        assertEquals("varchar(255)", columnElement.getAttribute("type"));
        constraintsElement = (Element) columnElement.getChildNodes().item(0);
        assertEquals("constraints", constraintsElement.getTagName());
        assertEquals(1, constraintsElement.getAttributes().getLength());
        assertEquals("true", constraintsElement.getAttribute("unique"));
    }

}