package liquibase.sqlgenerator.core;

public abstract class AddPrimaryKeyGeneratorTest {
//     @Test
//    public void execute_noSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AddPrimaryKeyStatement(null, TABLE_NAME, COLUMN_NAME, "PK_addpktest")) {
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
//                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
//                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());
//                    }
//
//                });
//    }
//
//    @Test
//    public void execute_altSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new AddPrimaryKeyStatement(TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME, "PK_addpktest")) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
//                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
//                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());
//                    }
//
//                });
//    }
//
//    @Test
//    public void execute_compundPKNoSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AddPrimaryKeyStatement(null, TABLE_NAME, COLUMN_NAME + "," + COLUMN2_NAME, "PK_addpktest")) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
//                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
//                        assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());
//                    }
//
//                });
//    }
//
//    @Test
//    public void execute_compundPKAltSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new AddPrimaryKeyStatement(TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME + "," + COLUMN2_NAME, "PK_addpktest")) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
//                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
//                        assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());
//                    }
//                });
//    }
//
//    @Test
//    public void execute_withTablespace() throws Exception {
//
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AddPrimaryKeyStatement(null, TABLE_NAME, COLUMN_NAME, "PK_addpktest").setTablespace(TestContext.ALT_TABLESPACE)) {
//
//                    protected boolean expectedException(Database database, DatabaseException exception) {
//                        return !database.supportsTablespaces();
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
//                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
//                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());
//                    }
//
//                });
//    }
}
