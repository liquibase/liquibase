package liquibase.sqlgenerator.core;

import liquibase.database.core.OracleDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.SetNullableStatement;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SetNullableGeneratorTest {

  private final SetNullableGenerator generator = new SetNullableGenerator();
  private final OracleDatabase oracle = new OracleDatabase();

  @Test
  public void testGenerateOracleNotNullSql() throws Exception {
    final Sql[] sqls = generator.generateSql(
        new SetNullableStatement(null, "schema_name", "table_name", "column_name", null, false), oracle, null);

    assertEquals(1, sqls.length);

    final Sql sql = sqls[0];

    assertEquals("ALTER TABLE schema_name.table_name MODIFY column_name NOT NULL", sql.toSql());
  }

  @Test
  public void testGenerateOracleNotNullSqlWithConstraintName() throws Exception {
    final Sql[] sqls = generator.generateSql(
        new SetNullableStatement(null, "schema_name", "table_name", "column_name", null, false, "constraint_name"),
        oracle, null);

    assertEquals(1, sqls.length);

    final Sql sql = sqls[0];

    assertEquals("ALTER TABLE schema_name.table_name MODIFY column_name CONSTRAINT constraint_name NOT NULL", sql.toSql());
  }

  ////    @Test
////    public void supports() throws Exception {
////        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
////
////            public void performTest(Database database) throws Exception {
////                SetNullableStatement statement = createGeneratorUnderTest();
////
////                if (database instanceof FirebirdDatabase) {
////                    assertFalse(statement.supportsDatabase(database));
////                } else {
////                    assertTrue(statement.supportsDatabase(database));
////                }
////            }
////        });
////    }
//
//    @Test
//    public void execute_nowNotNullNoSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new SetNullableStatement(null, NOTNULL_TABLE_NAME, COLUMN_NAME, "varchar(50)", false)) {
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertTrue(snapshot.getTable(NOTNULL_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertFalse(snapshot.getTable(NOTNULL_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
//                    }
//                });
//    }
//
//    @Test
//    public void execute_withSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new SetNullableStatement(TestContext.ALT_SCHEMA, NOTNULL_TABLE_NAME, COLUMN_NAME, "varchar(50)", false)) {
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertTrue(snapshot.getTable(NOTNULL_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertFalse(snapshot.getTable(NOTNULL_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
//                    }
//                });
//    }
//
//    @Test
//    public void execute_nowNullNoSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new SetNullableStatement(null, NULLABLE_TABLE_NAME, COLUMN_NAME, "varchar(50)", true)) {
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertFalse(snapshot.getTable(NULLABLE_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertTrue(snapshot.getTable(NULLABLE_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
//                    }
//                });
//    }
//
//    @Test
//    public void execute_nowNullableWithSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new SetNullableStatement(TestContext.ALT_SCHEMA, NULLABLE_TABLE_NAME, COLUMN_NAME, "varchar(50)", true)) {
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertFalse(snapshot.getTable(NULLABLE_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertTrue(snapshot.getTable(NULLABLE_TABLE_NAME).getColumn(COLUMN_NAME).isNullable());
//                    }
//                });
//    }

}
