package liquibase.change;

import liquibase.database.Database;
import liquibase.database.statement.AddUniqueConstraintStatement;
import liquibase.database.statement.SqlStatement;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import static org.junit.Assert.*;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

public class AddUniqueConstraintChangeTest extends AbstractChangeTest {

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Add Unique Constraint", new AddUniqueConstraintChange().getChangeDescription());
    }

    @Test
    public void generateStatement() throws Exception {

        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                AddUniqueConstraintChange change = new AddUniqueConstraintChange();
                change.setSchemaName("SCHEMA_NAME");
                change.setTableName("TABLE_NAME");
                change.setColumnNames("COL_HERE");
                change.setConstraintName("PK_NAME");
                change.setTablespace("TABLESPACE_NAME");

                SqlStatement[] sqlStatements = change.generateStatements(database);
                assertEquals(1, sqlStatements.length);
                assertTrue(sqlStatements[0] instanceof AddUniqueConstraintStatement);

                assertEquals("SCHEMA_NAME", ((AddUniqueConstraintStatement) sqlStatements[0]).getSchemaName());
                assertEquals("TABLE_NAME", ((AddUniqueConstraintStatement) sqlStatements[0]).getTableName());
                assertEquals("COL_HERE", ((AddUniqueConstraintStatement) sqlStatements[0]).getColumnNames());
                assertEquals("PK_NAME", ((AddUniqueConstraintStatement) sqlStatements[0]).getConstraintName());
                assertEquals("TABLESPACE_NAME", ((AddUniqueConstraintStatement) sqlStatements[0]).getTablespace());

            }
        });
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        AddUniqueConstraintChange change = new AddUniqueConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnNames("COL_HERE");

        assertEquals("Unique constraint added to TABLE_NAME(COL_HERE)", change.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        AddUniqueConstraintChange change = new AddUniqueConstraintChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnNames("COL_HERE");
        change.setConstraintName("PK_NAME");
        change.setTablespace("TABLESPACE_NAME");

        Element node = change.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("addUniqueConstraint", node.getTagName());
        assertEquals("SCHEMA_NAME", node.getAttribute("schemaName"));
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("COL_HERE", node.getAttribute("columnNames"));
        assertEquals("PK_NAME", node.getAttribute("constraintName"));
        assertEquals("TABLESPACE_NAME", node.getAttribute("tablespace"));
    }
}
