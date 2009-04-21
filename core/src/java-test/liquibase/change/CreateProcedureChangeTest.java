package liquibase.change;

import liquibase.database.Database;
import liquibase.database.OracleDatabase;
import liquibase.database.statement.RawSqlStatement;
import liquibase.database.statement.SqlStatement;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import static org.junit.Assert.*;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

public class CreateProcedureChangeTest extends AbstractChangeTest {

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Create Procedure", new CreateProcedureChange().getChangeName());
    }

    @Test
    public void generateStatement() throws Exception {
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                CreateProcedureChange change = new CreateProcedureChange();
                change.setProcedureBody("CREATE PROC PROCBODY HERE");
                change.setComments("Comments go here");

                SqlStatement[] sqlStatements = change.generateStatements(database);
                assertEquals(1, sqlStatements.length);
                assertTrue(sqlStatements[0] instanceof RawSqlStatement);

                assertEquals("CREATE PROC PROCBODY HERE", ((RawSqlStatement) sqlStatements[0]).getSqlStatement(database));

                if (database instanceof OracleDatabase) {
                    assertEquals("\n/", sqlStatements[0].getEndDelimiter(database));
                } else {
                    assertEquals(";", sqlStatements[0].getEndDelimiter(database));
                }
            }
        });
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        CreateProcedureChange refactoring = new CreateProcedureChange();
        assertEquals("Stored procedure created", refactoring.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        CreateProcedureChange refactoring = new CreateProcedureChange();
        refactoring.setProcedureBody("CREATE PROC PROCBODY HERE");
        refactoring.setComments("Comments go here");

        Element element = refactoring.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("createProcedure", element.getTagName());
        assertEquals("CREATE PROC PROCBODY HERE", element.getTextContent());
    }
}
