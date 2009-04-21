package liquibase.change;

import liquibase.database.Database;
import liquibase.database.statement.DropForeignKeyConstraintStatement;
import liquibase.database.statement.SqlStatement;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import static org.junit.Assert.*;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

public class DropForeignKeyConstraintChangeTest extends AbstractChangeTest {
 @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Drop Foreign Key Constraint", new DropForeignKeyConstraintChange().getChangeName());
    }

    @Test
    public void generateStatement() throws Exception {

        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                DropForeignKeyConstraintChange change = new DropForeignKeyConstraintChange();
                change.setBaseTableSchemaName("SCHEMA_NAME");
                change.setBaseTableName("TABLE_NAME");
                change.setConstraintName("FK_NAME");

                SqlStatement[] sqlStatements = change.generateStatements(database);
                assertEquals(1, sqlStatements.length);
                assertTrue(sqlStatements[0] instanceof DropForeignKeyConstraintStatement);

                assertEquals("SCHEMA_NAME", ((DropForeignKeyConstraintStatement) sqlStatements[0]).getBaseTableSchemaName());
                assertEquals("TABLE_NAME", ((DropForeignKeyConstraintStatement) sqlStatements[0]).getBaseTableName());
                assertEquals("FK_NAME", ((DropForeignKeyConstraintStatement) sqlStatements[0]).getConstraintName());
            }
        });
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        DropForeignKeyConstraintChange change = new DropForeignKeyConstraintChange();
        change.setBaseTableSchemaName("SCHEMA_NAME");
        change.setBaseTableName("TABLE_NAME");
        change.setConstraintName("FK_NAME");

        assertEquals("Foreign key FK_NAME dropped", change.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        DropForeignKeyConstraintChange change = new DropForeignKeyConstraintChange();
        change.setBaseTableSchemaName("SCHEMA_NAME");
        change.setBaseTableName("TABLE_NAME");
        change.setConstraintName("FK_NAME");

        Element node = change.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("dropForeignKeyConstraint", node.getTagName());
        assertEquals("SCHEMA_NAME", node.getAttribute("baseTableSchemaName"));
        assertEquals("TABLE_NAME", node.getAttribute("baseTableName"));
        assertEquals("FK_NAME", node.getAttribute("constraintName"));
    }
}
