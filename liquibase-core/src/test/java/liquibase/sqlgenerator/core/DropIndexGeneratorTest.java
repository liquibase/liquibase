package liquibase.sqlgenerator.core;

public class DropIndexGeneratorTest {
//    @Test
//    public void execute_defaultSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new DropIndexStatement(IDX_NAME, null, TABLE_NAME)) {
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getIndex(IDX_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
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
////                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
////                        //todo: how do we assert indexes within a schema snapshot?
//////                        assertNotNull(snapshot.getIndex(ALT_IDX_NAME));
////                    }
////
////                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
////                        //todo: how do we assert indexes within a schema snapshot?
//////                        assertNull(snapshot.getIndex(ALT_IDX_NAME));
////                    }
////
////                });
////    }

}
