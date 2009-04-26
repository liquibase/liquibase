package liquibase.change;

import liquibase.database.MockDatabase;
import liquibase.database.statement.AddColumnStatement;
import liquibase.database.statement.SqlStatement;
import static org.junit.Assert.*;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Tests for {@link AddColumnChange}
 */
public class AddColumnChangeTest extends AbstractChangeTest {

    @Test
    public void getRefactoringName() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        assertEquals("Add Column", refactoring.getDescription());
    }

    @Test
    public void generateStatement() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setSchemaName("SCHEMA");
        refactoring.setTableName("TAB");
        
        ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");

        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setNullable(Boolean.FALSE);

        column.setConstraints(constraints);

        refactoring.addColumn(column);

        //Add the second column def to the same refactoring
        column = new ColumnConfig();
        column.setName("NEWCOL2");
        column.setType("TYP2");
        column.setConstraints(new ConstraintsConfig());
        refactoring.addColumn(column);

        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());
        assertEquals(2, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof AddColumnStatement);
        assertTrue(sqlStatements[1] instanceof AddColumnStatement);

        assertEquals("SCHEMA", ((AddColumnStatement) sqlStatements[0]).getSchemaName());
        assertEquals("TAB", ((AddColumnStatement) sqlStatements[0]).getTableName());
        assertEquals("NEWCOL", ((AddColumnStatement) sqlStatements[0]).getColumnName());
        assertEquals("TYP", ((AddColumnStatement) sqlStatements[0]).getColumnType());
        assertFalse(((AddColumnStatement) sqlStatements[0]).isPrimaryKey());
        assertFalse(((AddColumnStatement) sqlStatements[0]).isNullable());

        assertEquals("SCHEMA", ((AddColumnStatement) sqlStatements[1]).getSchemaName());
        assertEquals("TAB", ((AddColumnStatement) sqlStatements[1]).getTableName());
        assertEquals("NEWCOL2", ((AddColumnStatement) sqlStatements[1]).getColumnName());
        assertEquals("TYP2", ((AddColumnStatement) sqlStatements[1]).getColumnType());
        assertTrue(((AddColumnStatement) sqlStatements[1]).isNullable());
    }

    @Test
    public void generateStatement_nullable() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setSchemaName("SCHEMA");
        refactoring.setTableName("TAB");
        ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");

        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setNullable(Boolean.TRUE);

        column.setConstraints(constraints);

        refactoring.addColumn(column);

        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof AddColumnStatement);

        assertEquals("SCHEMA", ((AddColumnStatement) sqlStatements[0]).getSchemaName());
        assertEquals("TAB", ((AddColumnStatement) sqlStatements[0]).getTableName());
        assertEquals("NEWCOL", ((AddColumnStatement) sqlStatements[0]).getColumnName());
        assertEquals("TYP", ((AddColumnStatement) sqlStatements[0]).getColumnType());
        assertFalse(((AddColumnStatement) sqlStatements[0]).isPrimaryKey());
        assertTrue(((AddColumnStatement) sqlStatements[0]).isNullable());
    }

    @Test
    public void generateStatement_notNull() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setSchemaName("SCHEMA");
        refactoring.setTableName("TAB");
        ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");

        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setNullable(Boolean.FALSE);

        column.setConstraints(constraints);

        refactoring.addColumn(column);

        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof AddColumnStatement);

        assertEquals("SCHEMA", ((AddColumnStatement) sqlStatements[0]).getSchemaName());
        assertEquals("TAB", ((AddColumnStatement) sqlStatements[0]).getTableName());
        assertEquals("NEWCOL", ((AddColumnStatement) sqlStatements[0]).getColumnName());
        assertEquals("TYP", ((AddColumnStatement) sqlStatements[0]).getColumnType());
        assertFalse(((AddColumnStatement) sqlStatements[0]).isPrimaryKey());
        assertFalse(((AddColumnStatement) sqlStatements[0]).isNullable());
    }

    @Test
    public void generateStatement_primaryKey() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setSchemaName("SCHEMA");
        refactoring.setTableName("TAB");
        ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");

        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setNullable(Boolean.FALSE);
        constraints.setPrimaryKey(Boolean.TRUE);

        column.setConstraints(constraints);

        refactoring.addColumn(column);

        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof AddColumnStatement);
        assertTrue(((AddColumnStatement) sqlStatements[0]).isPrimaryKey());
    }

    @Test
    public void generateStatement_autoIncrement() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setSchemaName("SCHEMA");
        refactoring.setTableName("TAB");
        ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");

        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setNullable(Boolean.FALSE);
        constraints.setPrimaryKey(Boolean.TRUE);
        column.setAutoIncrement(Boolean.TRUE);

        column.setConstraints(constraints);

        refactoring.addColumn(column);

        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof AddColumnStatement);
        assertTrue(((AddColumnStatement) sqlStatements[0]).isPrimaryKey());
        assertTrue(((AddColumnStatement) sqlStatements[0]).isAutoIncrement());
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setTableName("TAB");
        ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");
        refactoring.addColumn(column);

        assertEquals("Columns NEWCOL(TYP) added to TAB", refactoring.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setTableName("TAB");
        ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");
        refactoring.addColumn(column);

        Element node = refactoring.createNode(document);
        assertEquals("addColumn", node.getTagName());
        assertEquals("TAB", node.getAttribute("tableName"));

        NodeList columns = node.getElementsByTagName("column");
        assertEquals(1, columns.getLength());
        assertEquals("column", ((Element) columns.item(0)).getTagName());
        assertEquals("NEWCOL", ((Element) columns.item(0)).getAttribute("name"));
        assertEquals("TYP", ((Element) columns.item(0)).getAttribute("type"));

    }

//    @Test
//    public void sybaseNull() throws Exception {
//        AddColumnChange refactoring = new AddColumnChange();
//        refactoring.setTableName("TAB");
//        ColumnConfig column = new ColumnConfig();
//        column.setName("NEWCOL");
//        column.setType("TYP");
//        refactoring.addColumn(column);
//
//        SybaseDatabase db = new SybaseDatabase();
//        assertEquals("ALTER TABLE [TAB] ADD NEWCOL TYP NULL", refactoring.generateStatements(db)[0].getSqlStatement(db));
//    }

//    @Test
//    public void sybaseNotNull() throws Exception {
//        AddColumnChange refactoring = new AddColumnChange();
//        refactoring.setTableName("TAB");
//        ColumnConfig column = new ColumnConfig();
//        column.setName("NEWCOL");
//        column.setType("TYP");
//        refactoring.addColumn(column);
//
//        ConstraintsConfig constraints = new ConstraintsConfig();
//        constraints.setPrimaryKey(Boolean.FALSE);
//        constraints.setNullable(Boolean.FALSE);
//
//        column.setConstraints(constraints);
//
//        SybaseDatabase database = new SybaseDatabase();
//        assertEquals("ALTER TABLE [TAB] ADD NEWCOL TYP NOT NULL", refactoring.generateStatements(database)[0].getSqlStatement(database));
//
//    }

//    @Test
//    public void sybaseConstraintsNull() throws Exception {
//        AddColumnChange refactoring = new AddColumnChange();
//        refactoring.setTableName("TAB");
//        ColumnConfig column = new ColumnConfig();
//        column.setName("NEWCOL");
//        column.setType("TYP");
//        refactoring.addColumn(column);
//
//        ConstraintsConfig constraints = new ConstraintsConfig();
//        constraints.setPrimaryKey(Boolean.FALSE);
//        constraints.setNullable(Boolean.TRUE);
//
//        column.setConstraints(constraints);
//
//        SybaseDatabase database = new SybaseDatabase();
//        assertEquals("ALTER TABLE [TAB] ADD NEWCOL TYP NULL", refactoring.generateStatements(database)[0].getSqlStatement(database));
//
//    }
}
