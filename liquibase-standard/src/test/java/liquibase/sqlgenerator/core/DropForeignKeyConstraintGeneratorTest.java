package liquibase.sqlgenerator.core;

public abstract class DropForeignKeyConstraintGeneratorTest {
//    @Test
//    public void execute_defaultSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new DropForeignKeyConstraintStatement(null, TABLE_NAME, CONSTRAINT_NAME)) {
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getForeignKey(CONSTRAINT_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getForeignKey(CONSTRAINT_NAME));
//                    }
//
//                });
//    }
//
//    @Test
//    public void execute_altSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases (
//
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new DropForeignKeyConstraintStatement(TestContext.ALT_SCHEMA, TABLE_NAME, ALT_SCHEMA_NAME)) {
//
//                    protected boolean expectedException(Database database, DatabaseException exception) {
//                        return !database.supportsSchemas();
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        //fk constraint is not stored in the alt schema, how can we best test it?
////                        assertNotNull(snapshot.getForeignKey(ALT_SCHEMA_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        //fk constraint is not stored in the alt schema, how can we best test it?
////                        assertNull(snapshot.getForeignKey(ALT_SCHEMA_NAME));
//                    }
//
//                });
//    }

}
