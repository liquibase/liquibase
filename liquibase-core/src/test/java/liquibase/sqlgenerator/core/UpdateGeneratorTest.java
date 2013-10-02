package liquibase.sqlgenerator.core;

public abstract class UpdateGeneratorTest {
////    @Test
////    public void addNewColumnValue_nullValue() throws Exception {
////        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
////
////            public void performTest(Database database) {
////                UpdateStatement statement = new UpdateStatement(null, TABLE_NAME);
////                statement.addNewColumnValue(COLUMN_NAME, null);
////
////                assertEquals("UPDATE " + database.escapeTableName(null, TABLE_NAME) + " SET " + database.escapeColumnName(null, TABLE_NAME, COLUMN_NAME) + " = NULL", statement.getSqlStatement(database));
////            }
////        });
////    }
//
//    @Test
//    public void execute_altSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA,
//                new UpdateStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
//                        .addNewColumnValue(COLUMN_NAME, null)) {
//            protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                //nothing to test
//            }
//
//            protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                //nothing to test
//            }
//        });
//    }

}
