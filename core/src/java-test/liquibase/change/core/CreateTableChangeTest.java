package liquibase.change.core;

import liquibase.database.core.MockDatabase;
import liquibase.statement.CreateTableStatement;
import liquibase.statement.ForeignKeyConstraint;
import liquibase.statement.SqlStatement;
import liquibase.statement.UniqueConstraint;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropTableChange;
import liquibase.change.AbstractChangeTest;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.Change;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link CreateTableChange}
 */
public class CreateTableChangeTest extends AbstractChangeTest {

    private CreateTableChange change;

    @Before
    public void setUp() throws Exception {
        change = new CreateTableChange();
    }

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Create Table", change.getChangeMetaData().getDescription());
    }

    @Override
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

    @Override
    @Test
    public void getConfirmationMessage() throws Exception {
        change.setTableName("TAB_NAME");
        assertEquals("Table TAB_NAME created", change.getConfirmationMessage());
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
        assertEquals("2007-01-02", statement.getDefaultValue("id"));
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