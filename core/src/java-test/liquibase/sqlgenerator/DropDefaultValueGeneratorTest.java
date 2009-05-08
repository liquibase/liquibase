package liquibase.sqlgenerator;

public class DropDefaultValueGeneratorTest {
//    @Test
//    public void execute_defaultSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new DropDefaultValueStatement(null, TABLE_NAME, COLUMN_NAME, null)) {
//
//                    protected boolean supportsTest(Database database) {
//                        return !(database instanceof DerbyDatabase);
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNotNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertEquals(null, snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());
//                    }
//
//                });
//    }
//
//    @Test
//    public void execute_altSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new DropDefaultValueStatement(TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME, null)) {
//
//                    protected boolean supportsTest(Database database) {
//                        return !(database instanceof DerbyDatabase);
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNotNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());
//                    }
//                });
//    }

}
