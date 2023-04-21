package liquibase.sqlgenerator.core;

public abstract class CreateIndexGeneratorTest {
//    @Test
//    public void execute_singleColumnDefaultSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new CreateIndexStatement(INDEX_NAME, null, TABLE_NAME, IS_UNIQUE, COLUMN_NAME)) {
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getIndex(INDEX_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getIndex(INDEX_NAME));
//                        assertEquals(COLUMN_NAME.toUpperCase(), snapshot.getIndex(INDEX_NAME).getColumnNames().toUpperCase());
//                    }
//                });
//    }
//
//    @Test
//    public void execute_alternateSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new CreateIndexStatement(INDEX_NAME, TestContext.ALT_SCHEMA, TABLE_NAME, IS_UNIQUE, COLUMN_NAME)) {
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getIndex(INDEX_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        //todo: assert that index was created in the correct location.  What schema does it go in?
////                snapshot = new DatabaseSnapshotGenerator(database, TestContext.ALT_SCHEMA);
////                assertNotNull(snapshot.getIndex(INDEX_NAME));
////                assertEquals(COLUMN_NAME.toUpperCase(), snapshot.getIndex(INDEX_NAME).getColumnNames().toUpperCase());
//                    }
//                });
//    }
//
//    @Test
//    public void execute_alternateTablespace() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new CreateIndexStatement(INDEX_NAME, null, TABLE_NAME, IS_UNIQUE, COLUMN_NAME).setTablespace(TestContext.ALT_TABLESPACE)) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getIndex(INDEX_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getIndex(INDEX_NAME));
//                        assertEquals(COLUMN_NAME.toUpperCase(), snapshot.getIndex(INDEX_NAME).getColumnNames().toUpperCase());
//                        //todo: assert tablespace location
//                    }
//                });
//    }
//
//    @Test
//    public void execute_multiColumnDefaultSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new CreateIndexStatement(INDEX_NAME, null, TABLE_NAME, IS_UNIQUE, COLUMN_NAME, COLUMN_NAME2)) {
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getIndex(INDEX_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getIndex(INDEX_NAME));
//                        assertEquals((COLUMN_NAME + ", " + COLUMN_NAME2).toUpperCase(), snapshot.getIndex(INDEX_NAME).getColumnNames().toUpperCase());
//                    }
//                });
//    }

}
