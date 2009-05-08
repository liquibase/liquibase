package liquibase.statement.generator;

public class RenameColumnGeneratorTest {
////    @Test
////    public void isValidGenerator() throws Exception {
////        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
////            public void performTest(Database database) throws Exception {
////
////                if (database instanceof DB2Database
////                        || database instanceof CacheDatabase) {
////                    assertFalse(createGeneratorUnderTest().supportsDatabase(database));
////                } else {
////                    assertTrue(createGeneratorUnderTest().supportsDatabase(database));
////                }
////            }
////        });
////    }
//
//    @Test
//    public void execute_defaultSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new RenameColumnStatement(null, TABLE_NAME, COL_NAME, NEW_COL_NAME, DATA_TYPE)) {
//
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNotNull(snapshot.getTable(TABLE_NAME).getColumn(COL_NAME));
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COL_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(COL_NAME));
//                        assertNotNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COL_NAME));
//                    }
//
//                });
//    }
//
//     @Test
//    public void execute_altSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new RenameColumnStatement(TestContext.ALT_SCHEMA, TABLE_NAME, COL_NAME, NEW_COL_NAME, DATA_TYPE)) {
//
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNotNull(snapshot.getTable(TABLE_NAME).getColumn(COL_NAME));
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COL_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(COL_NAME));
//                        assertNotNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COL_NAME));
//                    }
//
//                });
//    }

}
