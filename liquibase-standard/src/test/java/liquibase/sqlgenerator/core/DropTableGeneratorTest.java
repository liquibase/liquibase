package liquibase.sqlgenerator.core;

public abstract class DropTableGeneratorTest {
//    @Test
//    public void execute_defaultSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new DropTableStatement(null, TABLE_NAME, false)) {
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getTable(TABLE_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME));
//                    }
//
//                });
//    }
//
//    @Test
//    public void execute_cascadeConstraints() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new DropTableStatement(null, TABLE_NAME, true)) {
//
//                    protected boolean expectedException(Database database, DatabaseException exception) {
//                        return database instanceof DerbyDatabase
//                                || database instanceof DB2Database
//                                || database instanceof FirebirdDatabase
//                                || database instanceof MSSQLDatabase;
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getTable(TABLE_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME));
//                    }
//
//                });
//    }
//
//    @Test
//    public void execute_altSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new DropTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME, false)) {
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getTable(TABLE_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME));
//                    }
//
//                });
//    }

}
