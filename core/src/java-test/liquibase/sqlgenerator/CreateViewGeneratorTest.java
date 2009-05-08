package liquibase.sqlgenerator;

public class CreateViewGeneratorTest {
//    @Test
//    public void execute_defaultSchema() throws Exception {
//        final String definition = "SELECT * FROM " + TABLE_NAME;
//
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new CreateViewStatement(null, VIEW_NAME, definition, false)) {
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNull(snapshot.getView(VIEW_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        View view = snapshot.getView(VIEW_NAME);
//                        assertNotNull(view);
//                        assertEquals(2, view.getColumns().size());
//                    }
//
//                });
//    }
//
//    @Test
//    public void execute_altSchema() throws Exception {
//        final String definition = "SELECT * FROM " + TestContext.ALT_SCHEMA+"."+TABLE_NAME;
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new CreateViewStatement(TestContext.ALT_SCHEMA, VIEW_NAME, definition, false)) {
//                    protected boolean supportsTest(Database database) {
//                        return !(database instanceof HsqlDatabase || database instanceof OracleDatabase); //don't know why oracle isn't working
//                    }
//
//                    protected boolean expectedException(Database database, JDBCException exception) {
//                        return !database.supportsSchemas();
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNull(snapshot.getView(VIEW_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        View view = snapshot.getView(VIEW_NAME);
//                        assertNotNull(view);
//                        assertEquals(2, view.getColumns().size());
//                    }
//
//                });
//    }

}
