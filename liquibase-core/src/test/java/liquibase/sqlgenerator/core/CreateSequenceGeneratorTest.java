package liquibase.sqlgenerator.core;

public abstract class CreateSequenceGeneratorTest {
//    //    @Test
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
//    public void execute() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new CreateSequenceStatement(null, SEQ_NAME)) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getSequence(SEQ_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                    }
//                });
//    }
//
//    @Test
//    public void execute_withSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new CreateSequenceStatement(TestContext.ALT_SCHEMA, SEQ_NAME)) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getSequence(SEQ_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                    }
//                });
//    }
//
//    @Test
//    public void execute_startValue() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new CreateSequenceStatement(null, SEQ_NAME).setStartValue(1000)) {
//
//                    protected boolean expectedException(Database database, DatabaseException exception) {
//                        return database instanceof FirebirdDatabase;
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getSequence(SEQ_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                        //todo: assert start value
//                    }
//                });
//    }
//
//    @Test
//    public void execute_incrementBy() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new CreateSequenceStatement(null, SEQ_NAME).setIncrementBy(5)) {
//
//                    protected boolean expectedException(Database database, DatabaseException exception) {
//                        return database instanceof FirebirdDatabase;
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getSequence(SEQ_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                        //todo: assert increment by value
//                    }
//
//                });
//    }
//
//    @Test
//    public void execute_minValue() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new CreateSequenceStatement(null, SEQ_NAME).setMinValue(15)) {
//
//                    protected boolean expectedException(Database database, DatabaseException exception) {
//                        return database instanceof FirebirdDatabase || database instanceof HsqlDatabase || database  instanceof H2Database;
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getSequence(SEQ_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                        //todo: assert min valuevalue
//                    }
//                });
//    }
//
//    @Test
//    public void execute_maxValue() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(new SqlStatementDatabaseTest(null, new CreateSequenceStatement(null, SEQ_NAME).setMaxValue(50)) {
//
//            protected boolean expectedException(Database database, DatabaseException exception) {
//                return database instanceof FirebirdDatabase || database instanceof HsqlDatabase || database  instanceof H2Database;
//            }
//
//            protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                assertNull(snapshot.getSequence(SEQ_NAME));
//            }
//
//            protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                assertNotNull(snapshot.getSequence(SEQ_NAME));
//                //todo: assert max value
//            }
//        });
//    }
//
//    @Test
//    public void execute_order() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(new SqlStatementDatabaseTest(null, new CreateSequenceStatement(null, SEQ_NAME).setOrdered(true)) {
//            protected boolean expectedException(Database database, DatabaseException exception) {
//                return !(database instanceof OracleDatabase || database instanceof DB2Database);
//            }
//
//            protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                assertNull(snapshot.getSequence(SEQ_NAME));
//            }
//
//            protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                assertNotNull(snapshot.getSequence(SEQ_NAME));
//                //todo: assert max value
//            }
//        });
//    }
}
