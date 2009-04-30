package liquibase.change;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.database.statement.AddPrimaryKeyStatement;
import liquibase.database.statement.ReorganizeTableStatement;
import liquibase.database.statement.SqlStatement;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import static org.junit.Assert.*;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

public class AddPrimaryKeyChangeTest extends AbstractChangeTest {

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Add Primary Key", new AddPrimaryKeyChange().getChangeMetaData().getDescription());
    }

    @Test
    public void generateStatement() throws Exception {

        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                AddPrimaryKeyChange change = new AddPrimaryKeyChange();
                change.setSchemaName("SCHEMA_NAME");
                change.setTableName("TABLE_NAME");
                change.setColumnNames("COL_HERE");
                change.setConstraintName("PK_NAME");
                change.setTablespace("TABLESPACE_NAME");

                SqlStatement[] sqlStatements = change.generateStatements(database);
                if (database instanceof DB2Database) {
                    assertEquals(2, sqlStatements.length);

                    assertTrue(sqlStatements[1] instanceof ReorganizeTableStatement);
                    assertEquals("SCHEMA_NAME", ((ReorganizeTableStatement) sqlStatements[1]).getSchemaName());
                    assertEquals("TABLE_NAME", ((ReorganizeTableStatement) sqlStatements[1]).getTableName());
                } else {
                    assertEquals(1, sqlStatements.length);
                }
                assertTrue(sqlStatements[0] instanceof AddPrimaryKeyStatement);

                assertEquals("SCHEMA_NAME", ((AddPrimaryKeyStatement) sqlStatements[0]).getSchemaName());
                assertEquals("TABLE_NAME", ((AddPrimaryKeyStatement) sqlStatements[0]).getTableName());
                assertEquals("COL_HERE", ((AddPrimaryKeyStatement) sqlStatements[0]).getColumnNames());
                assertEquals("PK_NAME", ((AddPrimaryKeyStatement) sqlStatements[0]).getConstraintName());
                assertEquals("TABLESPACE_NAME", ((AddPrimaryKeyStatement) sqlStatements[0]).getTablespace());

            }
        });
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        AddPrimaryKeyChange change = new AddPrimaryKeyChange();
        change.setTableName("TABLE_NAME");
        change.setColumnNames("COL_HERE");

        assertEquals("Primary key added to TABLE_NAME (COL_HERE)", change.getConfirmationMessage());
    }

}
