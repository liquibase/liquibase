package liquibase.sqlgenerator.core;

import java.util.Arrays;

import liquibase.database.core.MySQLDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.sqlgenerator.MockSqlGeneratorChain;
import liquibase.statement.core.DropColumnStatement;

import org.junit.Assert;
import org.junit.Test;

public class DropColumnGeneratorTest extends AbstractSqlGeneratorTest<DropColumnStatement> {

    public DropColumnGeneratorTest() throws Exception {
        super(new DropColumnGenerator());
    }

    @Override
    protected DropColumnStatement createSampleSqlStatement() {
        return new DropColumnStatement(null, null, "TEST_TABLE", "test_col");
    }

    @Test
    public void testDropMultipleColumnsMySQL() {
        DropColumnStatement drop = new DropColumnStatement(Arrays.asList(
            new DropColumnStatement(null, null, "TEST_TABLE", "col1"),
            new DropColumnStatement(null, null, "TEST_TABLE", "col2")
        ));

        Assert.assertFalse(generatorUnderTest.validate(drop, new MySQLDatabase(), new MockSqlGeneratorChain()).hasErrors());
        Sql[] sql = generatorUnderTest.generateSql(drop, new MySQLDatabase(), new MockSqlGeneratorChain());
        Assert.assertEquals(1, sql.length);
        Assert.assertEquals("ALTER TABLE TEST_TABLE DROP col1, DROP col2", sql[0].toSql());
        Assert.assertEquals("[DEFAULT, TEST_TABLE, TEST_TABLE.col1, TEST_TABLE.col2]", String.valueOf(sql[0].getAffectedDatabaseObjects()));
    }

////    @Test
////    public void supports() throws Exception {
////        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
////            public void performTest(Database database) throws Exception {
////                if (database instanceof SQLiteDatabase) {
////                    assertFalse(createGeneratorUnderTest().supportsDatabase(database));
////                } else {
////                    assertTrue(createGeneratorUnderTest().supportsDatabase(database));
////                }
////            }
////        });
////    }
//
//    @Test
//    public void execute_defaultSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new DropColumnStatement(null, TABLE_NAME, COLUMN_NAME)) {
//
//                    protected boolean supportsTest(Database database) {
//                        return !(database instanceof MSSQLDatabase); //for some reason, the metadata isn't updated by mssql
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME));
//                    }
//
//                });
//    }
//
//    @Test
//    public void execute_altSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new DropColumnStatement(TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME)) {
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME));
//                    }
//
//                });
//
//    }

}
