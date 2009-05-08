package liquibase.statement.generator;

public class AlterSequenceGeneratorTest {
////    @Test
////    public void isValidGenerator() throws Exception {
////        for (Database database : TestContext.getInstance().getAllDatabases()) {
////            if (database.supportsSequences()) {
////                assertTrue(createGeneratorUnderTest().supportsDatabase(database));
////            } else {
////                assertFalse(createGeneratorUnderTest().supportsDatabase(database));
////            }
////        }
////    }
//
//    @Test
//    public void execute_incrementBy() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AlterSequenceStatement(null, SEQ_NAME).setIncrementBy(5)) {
//                    protected boolean expectedException(Database database, JDBCException exception) {
//                        return database instanceof FirebirdDatabase || database instanceof HsqlDatabase;
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                        //todo: assert increment by is 1
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                        //todo: assert increment by value
//                    }
//                });
//    }
//
//    @Test
//    public void execute_minValue() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AlterSequenceStatement(null, SEQ_NAME).setMinValue(0)) {
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                        //todo; assert minValue is 1
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                        //todo: assert min valuevalue
//                    }
//                });
//    }
//
//    @Test
//    public void execute_maxValue() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AlterSequenceStatement(null, SEQ_NAME).setMaxValue(50)) {
//
//                    protected boolean expectedException(Database database, JDBCException exception) {
//                        return database instanceof FirebirdDatabase || database instanceof HsqlDatabase;
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                        //todo: assert initial max value
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                        //todo: assert max value
//                    }
//                });
//    }
//
//    @Test
//    public void execute_order() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AlterSequenceStatement(null, SEQ_NAME).setOrdered(true)) {
//
//                    protected boolean expectedException(Database database, JDBCException exception) {
//                        return !(database instanceof OracleDatabase || database instanceof DB2Database);
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                        //todo: assert order default
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                        //todo: assert max value
//                    }
//                });
//    }
//
//    @Test
//    public void execute_schemaSet() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new AlterSequenceStatement(TestContext.ALT_SCHEMA, SEQ_NAME).setIncrementBy(5)) {
//                    protected boolean expectedException(Database database, JDBCException exception) {
//                        return database instanceof FirebirdDatabase || database instanceof HsqlDatabase;
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                        //todo: assert increment by is 1
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                        //todo: assert increment by value
//                    }
//                });
//    }

}
