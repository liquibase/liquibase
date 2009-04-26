package liquibase.database.statement;

import liquibase.database.*;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.exception.JDBCException;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Test;

public class AddColumnGeneratorTest extends AbstractSqStatementTest {

    private static final String TABLE_NAME = "AddColTest";
    private static final String NEW_COLUMN_NAME = "NewCol";

    protected void setupDatabase(Database database) throws Exception {
        dropAndCreateTable(new CreateTableStatement(null, TABLE_NAME).addColumn("existingCol", "int"), database);
        dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME).addColumn("existingCol", "int"), database);
    }

    protected AddColumnStatement createGeneratorUnderTest() {
        return new AddColumnStatement(null, null, null, null, null);
    }

    @Test
    public void execute_stringDefault() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new AddColumnStatement(null, TABLE_NAME, NEW_COLUMN_NAME, "varchar(50)", "new default")) {
                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
                        assertNotNull(columnSnapshot);
                        assertEquals(NEW_COLUMN_NAME.toUpperCase(), columnSnapshot.getName().toUpperCase());
                        assertEquals("varchar".toUpperCase(), columnSnapshot.getTypeName().toUpperCase().replaceAll("VARCHAR2", "VARCHAR"));
                        assertEquals(50, columnSnapshot.getColumnSize());
                        assertEquals("new default", columnSnapshot.getDefaultValue());

                        assertEquals(true, columnSnapshot.isNullable());
                    }
                });
    }

    @Test
    public void execute_intDefault() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new AddColumnStatement(null, TABLE_NAME, NEW_COLUMN_NAME, "int", 42)) {
                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
                        assertNotNull(columnSnapshot);
                        assertEquals(NEW_COLUMN_NAME.toUpperCase(), columnSnapshot.getName().toUpperCase());
                        if (snapshot.getDatabase() instanceof OracleDatabase) {
                            assertEquals("NUMBER", columnSnapshot.getTypeName().toUpperCase());
                        } else {
                            assertTrue(columnSnapshot.getTypeName().toUpperCase().startsWith("INT"));
                        }
                        assertEquals(42, ((Number) columnSnapshot.getDefaultValue()).intValue());

                        assertEquals(true, columnSnapshot.isNullable());
                    }

                }

        );
    }

    @Test
    public void execute_floatDefault() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new AddColumnStatement(null, TABLE_NAME, NEW_COLUMN_NAME, "float", 42.5)) {
                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
                        assertNotNull(columnSnapshot);
                        assertEquals(NEW_COLUMN_NAME.toUpperCase(), columnSnapshot.getName().toUpperCase());
                        assertEquals(new Double(42.5), new Double(((Number) columnSnapshot.getDefaultValue()).doubleValue()));

                        assertEquals(true, columnSnapshot.isNullable());
                    }
                });
    }

    @Test
    public void execute_notNull() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new AddColumnStatement(null, TABLE_NAME, NEW_COLUMN_NAME, "int", 42, new NotNullConstraint())) {
                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
                        assertNotNull(columnSnapshot);
                        assertEquals(false, columnSnapshot.isNullable());
                    }
                }

        );
    }

    @Test
    public void execute_primaryKey_nonAutoIncrement() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new AddColumnStatement(null, TABLE_NAME, NEW_COLUMN_NAME, "int", null, new PrimaryKeyConstraint())) {

                    protected boolean expectedException(Database database, JDBCException exception) {
                        return (database instanceof DB2Database
                                || database instanceof DerbyDatabase
                                || database instanceof H2Database
                                || database instanceof CacheDatabase);
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
                        assertNotNull(columnSnapshot);
                        assertEquals(false, columnSnapshot.isNullable());
                        assertTrue(columnSnapshot.isPrimaryKey());
                        assertEquals(false, columnSnapshot.isAutoIncrement());
                    }
                });
    }

    @Test
    public void execute_altSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new AddColumnStatement(TestContext.ALT_SCHEMA, TABLE_NAME, NEW_COLUMN_NAME, "varchar(50)", "new default")) {
                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
                        assertNotNull(columnSnapshot);
                        assertEquals(NEW_COLUMN_NAME.toUpperCase(), columnSnapshot.getName().toUpperCase());
                        assertEquals("new default", columnSnapshot.getDefaultValue());

                        assertEquals(true, columnSnapshot.isNullable());
                    }

                });
    }

    @Test
      public void execute_primaryKeyAutoIncrement() throws Exception {
          new DatabaseTestTemplate().testOnAvailableDatabases(
                  new SqlStatementDatabaseTest(null, new AddColumnStatement(null, TABLE_NAME, NEW_COLUMN_NAME, "int", null, new PrimaryKeyConstraint(), new AutoIncrementConstraint())) {

                      protected boolean expectedException(Database database, JDBCException exception) {
                          return (database instanceof DB2Database
                                  || database instanceof DerbyDatabase
                                  || database instanceof H2Database
                                  || database instanceof CacheDatabase
                                    || !database.supportsAutoIncrement());
                      }

                      protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                          assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
                      }

                      protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                          Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
                          assertNotNull(columnSnapshot);
                          assertEquals(false, columnSnapshot.isNullable());
                          assertTrue(columnSnapshot.isPrimaryKey());
                          assertEquals(true, columnSnapshot.isAutoIncrement());
                      }
                  });
      }

}
