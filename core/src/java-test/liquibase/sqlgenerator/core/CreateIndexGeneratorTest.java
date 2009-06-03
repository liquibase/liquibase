package liquibase.sqlgenerator.core;

public class CreateIndexGeneratorTest {
//    @Test
//    public void execute_singleColumnDefaultSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new CreateIndexStatement(INDEX_NAME, null, TABLE_NAME, IS_UNIQUE, COLUMN_NAME)) {
//
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNull(snapshot.getIndex(INDEX_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
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
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNull(snapshot.getIndex(INDEX_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        //todo: assert that index was created in the correct location.  What schema does it go in?
////                snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
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
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNull(snapshot.getIndex(INDEX_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
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
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNull(snapshot.getIndex(INDEX_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNotNull(snapshot.getIndex(INDEX_NAME));
//                        assertEquals((COLUMN_NAME + ", " + COLUMN_NAME2).toUpperCase(), snapshot.getIndex(INDEX_NAME).getColumnNames().toUpperCase());
//                    }
//                });
//    }

}
