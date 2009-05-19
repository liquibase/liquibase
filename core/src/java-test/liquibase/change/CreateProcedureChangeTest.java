package liquibase.change;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public abstract class CreateProcedureChangeTest extends AbstractChangeTest {

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Create Procedure", new CreateProcedureChange().getChangeMetaData().getDescription());
    }

//    @Test
//    public void generateStatement() throws Exception {
//        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
//            public void performTest(Database database) throws Exception {
//                CreateProcedureChange change = new CreateProcedureChange();
//                change.setProcedureBody("CREATE PROC PROCBODY HERE");
//                change.setComments("Comments go here");
//
//                SqlStatement[] sqlStatements = change.generateStatements(database);
//                assertEquals(1, sqlStatements.length);
//                assertTrue(sqlStatements[0] instanceof RawSqlStatement);
//
//                assertEquals("CREATE PROC PROCBODY HERE", ((RawSqlStatement) sqlStatements[0]).getSqlStatement(database));
//
//                if (database instanceof OracleDatabase) {
//                    assertEquals("\n/", sqlStatements[0].getEndDelimiter(database));
//                } else {
//                    assertEquals(";", sqlStatements[0].getEndDelimiter(database));
//                }
//            }
//        });
//    }

    @Override
    @Test
    public void getConfirmationMessage() throws Exception {
        CreateProcedureChange refactoring = new CreateProcedureChange();
        assertEquals("Stored procedure created", refactoring.getConfirmationMessage());
    }
}
