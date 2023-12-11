package liquibase.sqlgenerator.core;

public abstract class InsertGeneratorTest {
//    @Test
//    public void execute_defaultSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null,
//                        new InsertStatement(null, TABLE_NAME)
//                                .addColumnValue(VARCHAR_COL_NAME, "new value")) {
//
//                    private int oldCount;
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) throws Exception {
//                        oldCount = new Executor(snapshot.getDatabase()).queryForInt(new RawSqlStatement("select count(*) from " + TABLE_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) throws Exception {
//                        int newCount = new Executor(snapshot.getDatabase()).queryForInt(new RawSqlStatement("select count(*) from " + TABLE_NAME));
//                        assertEquals(oldCount + 1, newCount);
//                    }
//
//                });
//    }
//
//    @Test
//    public void execute_altSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA,
//                        new InsertStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
//                                .addColumnValue(VARCHAR_COL_NAME, "new value")) {
//
//                    private int oldCount;
//
//                    protected boolean supportsTest(Database database) {
//                        return !(database instanceof FirebirdDatabase);
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) throws Exception {
//                        oldCount = new Executor(snapshot.getDatabase()).queryForInt(new RawSqlStatement("select count(*) from " + TestContext.ALT_SCHEMA + "." + TABLE_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) throws Exception {
//                        int newCount = new Executor(snapshot.getDatabase()).queryForInt(new RawSqlStatement("select count(*) from " + TestContext.ALT_SCHEMA + "." + TABLE_NAME));
//                        assertEquals(oldCount + 1, newCount);
//                    }
//
//                });
//    }
//
//    @Test
//    public void execute_multiColumns() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null,
//                        new InsertStatement(null, TABLE_NAME)
//                                .addColumnValue(VARCHAR_COL_NAME, "new value")
//                                .addColumnValue(DATE_COL_NAME, new java.sql.Date(new java.util.Date().getTime()))
//                                .addColumnValue(INT_COL_NAME, 42)
//                                .addColumnValue(FLOAT_COL_NAME, 123.456)) {
//
//                    private int oldCount;
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) throws Exception {
//                        oldCount = new Executor(snapshot.getDatabase()).queryForInt(new RawSqlStatement("select count(*) from " + TABLE_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) throws Exception {
//                        int newCount = new Executor(snapshot.getDatabase()).queryForInt(new RawSqlStatement("select count(*) from " + TABLE_NAME));
//                        assertEquals(oldCount + 1, newCount);
//                    }
//
//                });
//    }

}
