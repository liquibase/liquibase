package liquibase.sqlgenerator;

public class DropColumnGeneratorTest {
////    @Test
////    public void isValidGenerator() throws Exception {
////        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
////            public void performTest(Database database) throws Exception {
////                if (database instanceof SQLiteDatabase) {
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
//                new SqlStatementDatabaseTest(null, new DropColumnStatement(null, TABLE_NAME, COLUMN_NAME)) {
//
//                    protected boolean supportsTest(Database database) {
//                        return !(database instanceof MSSQLDatabase); //for some reason, the metadata isn't updated by mssql
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNotNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME));
//                    }
//
//                });
//    }
//
//    @Test
//    public void execute_altSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new DropColumnStatement(TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME)) {
//
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNotNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME));
//                    }
//
//                });
//
//    }

}
