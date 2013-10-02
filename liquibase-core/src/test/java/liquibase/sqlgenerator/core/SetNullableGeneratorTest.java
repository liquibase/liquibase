package liquibase.sqlgenerator.core;

public abstract class SetNullableGeneratorTest {
////    @Test
////    public void supports() throws Exception {
////        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
////
////            public void performTest(Database database) throws Exception {
////                SetNullableStatement statement = createGeneratorUnderTest();
////
////                if (database instanceof FirebirdDatabase) {
////                    assertFalse(statement.supportsDatabase(database));
////                } else {
////                    assertTrue(statement.supportsDatabase(database));
////                }
////            }
////        });
////    }
//
//    @Test
//    public void execute_nowNotNullNoSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new SetNullableStatement(null, NOTNULL_TABLE_NAME, COLUMN_NAME, "varchar(50)", false)) {
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertTrue(snapshot.getTable(NOTNULL_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertFalse(snapshot.getTable(NOTNULL_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
//                    }
//                });
//    }
//
//    @Test
//    public void execute_withSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new SetNullableStatement(TestContext.ALT_SCHEMA, NOTNULL_TABLE_NAME, COLUMN_NAME, "varchar(50)", false)) {
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertTrue(snapshot.getTable(NOTNULL_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertFalse(snapshot.getTable(NOTNULL_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
//                    }
//                });
//    }
//
//    @Test
//    public void execute_nowNullNoSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new SetNullableStatement(null, NULLABLE_TABLE_NAME, COLUMN_NAME, "varchar(50)", true)) {
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertFalse(snapshot.getTable(NULLABLE_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertTrue(snapshot.getTable(NULLABLE_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
//                    }
//                });
//    }
//
//    @Test
//    public void execute_nowNullableWithSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new SetNullableStatement(TestContext.ALT_SCHEMA, NULLABLE_TABLE_NAME, COLUMN_NAME, "varchar(50)", true)) {
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertFalse(snapshot.getTable(NULLABLE_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertTrue(snapshot.getTable(NULLABLE_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
//                    }
//                });
//    }

}
