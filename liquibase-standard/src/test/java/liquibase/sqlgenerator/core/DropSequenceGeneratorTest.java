package liquibase.sqlgenerator.core;

public abstract class DropSequenceGeneratorTest {
//    private static final String SEQ_NAME = "DROPTEST_SEQ";
//
//    protected void setupDatabase(Database database) throws Exception {
//                dropAndCreateSequence(new CreateSequenceStatement(null, SEQ_NAME), database);
//                dropAndCreateSequence(new CreateSequenceStatement(TestContext.ALT_SCHEMA, SEQ_NAME), database);
//    }
//
////    @Test
////    public void supports() throws Exception {
////        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
////            public void performTest(Database database) throws Exception {
////                if (database.supportsSequences()) {
////                    assertTrue(createGeneratorUnderTest().supportsDatabase(database));
////                } else {
////                    assertFalse(createGeneratorUnderTest().supportsDatabase(database));
////                }
////            }
////        });
////    }
//
//    @Test
//    public void execute_noSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new DropSequenceStatement(null, SEQ_NAME)) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getSequence(SEQ_NAME));
//                    }
//                });
//    }
//
//    @Test
//    public void execute_withSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new DropSequenceStatement(TestContext.ALT_SCHEMA, SEQ_NAME)) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getSequence(SEQ_NAME));
//                    }
//                });
//    }

}
