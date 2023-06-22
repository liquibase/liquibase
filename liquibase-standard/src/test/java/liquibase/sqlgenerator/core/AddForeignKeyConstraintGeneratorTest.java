package liquibase.sqlgenerator.core;

public abstract class AddForeignKeyConstraintGeneratorTest {
//    @Test
//    public void execute() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AddForeignKeyConstraintStatement(FK_NAME,
//                        null, BASE_TABLE_NAME, BASE_COLUMN_NAME,
//                        null, REF_TABLE_NAME, REF_COL_NAME)) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getForeignKey(FK_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        ForeignKey fkSnapshot = snapshot.getForeignKey(FK_NAME);
//                        assertNotNull(fkSnapshot);
//                        assertEquals(BASE_TABLE_NAME.toUpperCase(), fkSnapshot.getForeignKeyTable().getName().toUpperCase());
//                        assertEquals(BASE_COLUMN_NAME.toUpperCase(), fkSnapshot.getForeignKeyColumns().toUpperCase());
//                        assertEquals(REF_TABLE_NAME.toUpperCase(), fkSnapshot.getPrimaryKeyTable().getName().toUpperCase());
//                        assertEquals(REF_COL_NAME.toUpperCase(), fkSnapshot.getPrimaryKeyColumns().toUpperCase());
//                        assertFalse(fkSnapshot.isDeferrable());
//                        assertFalse(fkSnapshot.isInitiallyDeferred());
//                    }
//
//                });
//    }
//
//    @Test
//    public void execute_deferrable() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AddForeignKeyConstraintStatement(FK_NAME,
//                        null, BASE_TABLE_NAME, BASE_COLUMN_NAME,
//                        null, REF_TABLE_NAME, REF_COL_NAME)
//                        .setDeferrable(true)
//                        .setInitiallyDeferred(true)) {
//                    protected boolean expectedException(Database database, DatabaseException exception) {
//                        return !database.supportsInitiallyDeferrableColumns();
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getForeignKey(FK_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        ForeignKey fkSnapshot = snapshot.getForeignKey(FK_NAME);
//                        assertNotNull(fkSnapshot);
//                        assertEquals(BASE_TABLE_NAME.toUpperCase(), fkSnapshot.getForeignKeyTable().getName().toUpperCase());
//                        assertEquals(BASE_COLUMN_NAME.toUpperCase(), fkSnapshot.getForeignKeyColumns().toUpperCase());
//                        assertEquals(REF_TABLE_NAME.toUpperCase(), fkSnapshot.getPrimaryKeyTable().getName().toUpperCase());
//                        assertEquals(REF_COL_NAME.toUpperCase(), fkSnapshot.getPrimaryKeyColumns().toUpperCase());
//                        assertTrue(fkSnapshot.isDeferrable());
//                        assertTrue(fkSnapshot.isInitiallyDeferred());
//                    }
//                });
//    }
//
//    @Test
//    public void execute_deleteCascade() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(new SqlStatementDatabaseTest(null, new AddForeignKeyConstraintStatement(FK_NAME,
//                null, BASE_TABLE_NAME, BASE_COLUMN_NAME,
//                null, REF_TABLE_NAME, REF_COL_NAME).setDeleteRule(DatabaseMetaData.importedKeyCascade)) {
//            protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                assertNull(snapshot.getForeignKey(FK_NAME));
//            }
//
//            protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                ForeignKey fkSnapshot = snapshot.getForeignKey(FK_NAME);
//                assertNotNull(fkSnapshot);
//                assertEquals(BASE_TABLE_NAME.toUpperCase(), fkSnapshot.getForeignKeyTable().getName().toUpperCase());
//                assertEquals(BASE_COLUMN_NAME.toUpperCase(), fkSnapshot.getForeignKeyColumns().toUpperCase());
//                assertEquals(REF_TABLE_NAME.toUpperCase(), fkSnapshot.getPrimaryKeyTable().getName().toUpperCase());
//                assertEquals(REF_COL_NAME.toUpperCase(), fkSnapshot.getPrimaryKeyColumns().toUpperCase());
//                assertFalse(fkSnapshot.isDeferrable());
//                assertFalse(fkSnapshot.isInitiallyDeferred());
//            }
//
//        });
//    }
//
//    @Test
//    public void execute_altSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new AddForeignKeyConstraintStatement(FK_NAME,
//                        TestContext.ALT_SCHEMA, BASE_TABLE_NAME, BASE_COLUMN_NAME,
//                        TestContext.ALT_SCHEMA, REF_TABLE_NAME, REF_COL_NAME)) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getForeignKey(FK_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        ForeignKey fkSnapshot = snapshot.getForeignKey(FK_NAME);
//                        assertNotNull(fkSnapshot);
//                        assertEquals(BASE_TABLE_NAME.toUpperCase(), fkSnapshot.getForeignKeyTable().getName().toUpperCase());
//                        assertEquals(BASE_COLUMN_NAME.toUpperCase(), fkSnapshot.getForeignKeyColumns().toUpperCase());
//                        assertEquals(REF_TABLE_NAME.toUpperCase(), fkSnapshot.getPrimaryKeyTable().getName().toUpperCase());
//                        assertEquals(REF_COL_NAME.toUpperCase(), fkSnapshot.getPrimaryKeyColumns().toUpperCase());
//                        assertFalse(fkSnapshot.isDeferrable());
//                        assertFalse(fkSnapshot.isInitiallyDeferred());
//                    }
//
//                });
//    }

}
