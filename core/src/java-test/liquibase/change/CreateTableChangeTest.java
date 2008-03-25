package liquibase.change;

import liquibase.database.MockDatabase;
import liquibase.database.sql.CreateTableStatement;
import liquibase.database.sql.ForeignKeyConstraint;
import liquibase.database.sql.SqlStatement;
import liquibase.database.sql.UniqueConstraint;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Set;

/**
 * Tests for {@link CreateTableChange}
 */
public class CreateTableChangeTest extends AbstractChangeTest {

    private CreateTableChange change;

    @Before
    public void setUp() throws Exception {
        change = new CreateTableChange();
    }

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Create Table", change.getChangeName());
    }

    @Test
    public void generateStatement() throws Exception {
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


        SqlStatement[] statements = change.generateStatements(new MockDatabase());
        assertEquals(1, statements.length);
        assertTrue(statements[0] instanceof CreateTableStatement);
        CreateTableStatement statement = (CreateTableStatement) statements[0];
        assertEquals("TABLE_NAME", statement.getTableName());
        assertTrue(statement.getColumns().contains("id"));
        assertTrue(statement.getColumns().contains("state_id"));
        assertTrue(statement.getColumns().contains("phone"));
        assertTrue(statement.getColumns().contains("phone2"));

        assertEquals(1, statement.getPrimaryKeyConstraint().getColumns().size());
        assertEquals("id", statement.getPrimaryKeyConstraint().getColumns().iterator().next());

        assertEquals(1, statement.getUniqueConstraints().size());
        UniqueConstraint uniqueConstraint = statement.getUniqueConstraints().iterator().next();
        assertEquals(1, uniqueConstraint.getColumns().size());
        assertEquals("phone2", uniqueConstraint.getColumns().iterator().next());

        assertEquals(2, statement.getNotNullColumns().size());

        assertEquals(1, statement.getForeignKeyConstraints().size());
        ForeignKeyConstraint keyConstraint = statement.getForeignKeyConstraints().iterator().next();
        assertEquals("fk_tab_ref", keyConstraint.getForeignKeyName());
        assertEquals("state_id", keyConstraint.getColumn());
        assertEquals("state(id)", keyConstraint.getReferences());
        assertTrue(keyConstraint.isDeferrable());
        assertTrue(keyConstraint.isInitiallyDeferred());
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        change.setTableName("TAB_NAME");
        assertEquals("Table TAB_NAME created", change.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
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

    @Test
    public void defaultValue_none() throws Exception {
        CreateTableChange change = new CreateTableChange();
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setName("id");
        change.addColumn(columnConfig);

        CreateTableStatement statement = (CreateTableStatement) change.generateStatements(new MockDatabase())[0];
        assertNull(statement.getDefaultValue("id"));
    }
    
    @Test
    public void defaultValue_string() throws Exception {
        CreateTableChange change = new CreateTableChange();
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setName("id");
        columnConfig.setDefaultValue("DEFAULTVALUE");
        change.addColumn(columnConfig);

        CreateTableStatement statement = (CreateTableStatement) change.generateStatements(new MockDatabase())[0];
        assertEquals("'DEFAULTVALUE'", statement.getDefaultValue("id"));
    }

    @Test
    public void defaultValue_boolean() throws Exception {
        CreateTableChange change = new CreateTableChange();
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setName("id");
        columnConfig.setDefaultValueBoolean(Boolean.TRUE);
        change.addColumn(columnConfig);

        CreateTableStatement statement = (CreateTableStatement) change.generateStatements(new MockDatabase())[0];
        assertEquals("'"+new MockDatabase().getTrueBooleanValue()+"'", statement.getDefaultValue("id"));
    }

    @Test
    public void defaultValue_numeric() throws Exception {
        CreateTableChange change = new CreateTableChange();
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setName("id");
        columnConfig.setDefaultValueNumeric("42");
        change.addColumn(columnConfig);

        CreateTableStatement statement = (CreateTableStatement) change.generateStatements(new MockDatabase())[0];
        assertEquals("42", statement.getDefaultValue("id"));
    }

    @Test
    public void defaultValue_date() throws Exception {
        CreateTableChange change = new CreateTableChange();
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setName("id");
        columnConfig.setDefaultValueDate("2007-01-02");
        change.addColumn(columnConfig);

        CreateTableStatement statement = (CreateTableStatement) change.generateStatements(new MockDatabase())[0];
        assertEquals("'2007-01-02'", statement.getDefaultValue("id"));
    }

    @Test
    public void createInverse() {
        CreateTableChange change = new CreateTableChange();
        change.setTableName("TestTable");

        Change[] inverses = change.createInverses();
        assertEquals(1, inverses.length);
        assertTrue(inverses[0] instanceof DropTableChange);
        assertEquals("TestTable", ((DropTableChange) inverses[0]).getTableName());
    }

    @Test
    public void tableSpace_none() throws Exception {
        CreateTableChange change = new CreateTableChange();

        CreateTableStatement statement = (CreateTableStatement) change.generateStatements(new MockDatabase())[0];
        assertNull(statement.getTablespace());
    }

    @Test
    public void tableSpace_set() throws Exception {
        CreateTableChange change = new CreateTableChange();
        change.setTablespace("TESTTABLESPACE");

        CreateTableStatement statement = (CreateTableStatement) change.generateStatements(new MockDatabase())[0];
        assertEquals("TESTTABLESPACE", statement.getTablespace());
    }

    @Test
    public void getAffectedDatabaseObjects() {
        CreateTableChange change = new CreateTableChange();
        change.setTableName("testTable");

        ColumnConfig column = new ColumnConfig();
        column.setName("id");
        change.addColumn(column);

        column = new ColumnConfig();
        column.setName("id2");
        change.addColumn(column);

        Set<DatabaseObject> affectedDatabaseObjects = change.getAffectedDatabaseObjects();
        assertEquals(3, affectedDatabaseObjects.size());
        for (DatabaseObject object : affectedDatabaseObjects) {
            if (object instanceof Table) {
                assertEquals("testTable", ((Table) object).getName());
            } else {
                assertEquals("testTable", ((Column) object).getTable().getName());
                String columnName = ((Column) object).getName();
                assertTrue(columnName.equals("id") || columnName.equals("id2"));
            }
        }
    }

    @Test
    public void foreignKey_deferrable() throws Exception {
        CreateTableChange change = new CreateTableChange();
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setName("id");
        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setForeignKeyName("fk_test");
        constraints.setReferences("test(id)");
        constraints.setDeferrable(true);
        constraints.setInitiallyDeferred(true);
        columnConfig.setConstraints(constraints);
        change.addColumn(columnConfig);

        CreateTableStatement statement = (CreateTableStatement) change.generateStatements(new MockDatabase())[0];
        ForeignKeyConstraint keyConstraint = statement.getForeignKeyConstraints().iterator().next();
        assertTrue(keyConstraint.isDeferrable());
        assertTrue(keyConstraint.isInitiallyDeferred());
    }

    @Test
    public void foreignKey_notDeferrable() throws Exception {
        CreateTableChange change = new CreateTableChange();
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setName("id");
        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setForeignKeyName("fk_test");
        constraints.setReferences("test(id)");
        constraints.setDeferrable(false);
        constraints.setInitiallyDeferred(false);
        columnConfig.setConstraints(constraints);
        change.addColumn(columnConfig);

        CreateTableStatement statement = (CreateTableStatement) change.generateStatements(new MockDatabase())[0];
        ForeignKeyConstraint keyConstraint = statement.getForeignKeyConstraints().iterator().next();
        assertFalse(keyConstraint.isDeferrable());
        assertFalse(keyConstraint.isInitiallyDeferred());
    }

    @Test
    public void foreignKey_defaultDeferrable() throws Exception {
        CreateTableChange change = new CreateTableChange();
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setName("id");
        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setReferences("test(id)");
        constraints.setForeignKeyName("fk_test");
        columnConfig.setConstraints(constraints);
        change.addColumn(columnConfig);

        CreateTableStatement statement = (CreateTableStatement) change.generateStatements(new MockDatabase())[0];
        ForeignKeyConstraint keyConstraint = statement.getForeignKeyConstraints().iterator().next();
        assertFalse(keyConstraint.isDeferrable());
        assertFalse(keyConstraint.isInitiallyDeferred());
    }
}