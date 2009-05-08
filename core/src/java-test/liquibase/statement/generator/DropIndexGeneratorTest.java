package liquibase.statement.generator;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.statement.DropIndexStatement;
import liquibase.database.structure.DatabaseSnapshot;

public class DropIndexGeneratorTest {
//    @Test
//    public void execute_defaultSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new DropIndexStatement(IDX_NAME, null, TABLE_NAME)) {
//
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNotNull(snapshot.getIndex(IDX_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNull(snapshot.getIndex(IDX_NAME));
//                    }
//
//                });
//    }
//
//    //todo: issues with schemas on some databases
////    @Test
////    public void execute_altSchema() throws Exception {
////        new DatabaseTestTemplate().testOnAvailableDatabases(
////                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new DropIndexStatement(ALT_IDX_NAME, TestContext.ALT_SCHEMA, TABLE_NAME)) {
////
////                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
////                        //todo: how do we assert indexes within a schema snapshot?
//////                        assertNotNull(snapshot.getIndex(ALT_IDX_NAME));
////                    }
////
////                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
////                        //todo: how do we assert indexes within a schema snapshot?
//////                        assertNull(snapshot.getIndex(ALT_IDX_NAME));
////                    }
////
////                });
////    }

}
