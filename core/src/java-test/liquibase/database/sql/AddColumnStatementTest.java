package liquibase.database.sql;

import liquibase.database.*;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.template.JdbcTemplate;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class AddColumnStatementTest extends AbstractSqlStatementTest {

    private static final String TABLE_NAME = "AddColTest";
    private static final String NEW_COLUMN_NAME = "NewCol";

    @Before
    public void dropAndCreateTable() throws Exception {
        for (Database database : TestContext.getInstance().getAvailableDatabases()) {
            dropAndCreateTable(new CreateTableStatement(TABLE_NAME).addColumn("existingCol", "int"), database);

            if (database.supportsSchemas()) {
                dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME).addColumn("existingCol", "int"), database);
            }
        }

    }

    @Test
    public void getEndDelimiter() throws Exception {
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                assertEquals(";", new AddColumnStatement(null, null, null, null, null).getEndDelimiter(database));
            }
        });
    }

    @Test
    public void supportsDatabase() throws Exception {
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                assertTrue(new AddColumnStatement(null, null, null, null, null).supportsDatabase(database));
            }
        });
    }

    @Test
    public void execute_stringDefault() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));

                new JdbcTemplate(database).execute(new AddColumnStatement(null, TABLE_NAME, NEW_COLUMN_NAME, "varchar(50)", "new default"));

                snapshot = new DatabaseSnapshot(database);
                Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
                assertNotNull(columnSnapshot);
                assertEquals(NEW_COLUMN_NAME.toUpperCase(), columnSnapshot.getName().toUpperCase());
                if (database instanceof OracleDatabase) {
                    assertEquals("varchar2".toUpperCase(), columnSnapshot.getTypeName().toUpperCase());
                } else {
                    assertEquals("varchar".toUpperCase(), columnSnapshot.getTypeName().toUpperCase());
                }
                assertEquals(50, columnSnapshot.getColumnSize());
                assertEquals("new default", columnSnapshot.getDefaultValue());

                assertEquals(true, columnSnapshot.isNullable());

            }
        });
    }

    @Test
    public void execute_intDefault() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));

                new JdbcTemplate(database).execute(new AddColumnStatement(null, TABLE_NAME, NEW_COLUMN_NAME, "int", 42));

                snapshot = new DatabaseSnapshot(database);
                Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
                assertNotNull(columnSnapshot);
                assertEquals(NEW_COLUMN_NAME.toUpperCase(), columnSnapshot.getName().toUpperCase());
                if (database instanceof OracleDatabase) {
                    assertEquals("NUMBER", columnSnapshot.getTypeName().toUpperCase());
                } else {
                    assertTrue(columnSnapshot.getTypeName().toUpperCase().startsWith("INT"));
                }
                assertEquals(42, ((Number) columnSnapshot.getDefaultValue()).intValue());

                assertEquals(true, columnSnapshot.isNullable());

            }
        });
    }

    @Test
    public void execute_floatDefault() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));

                new JdbcTemplate(database).execute(new AddColumnStatement(null, TABLE_NAME, NEW_COLUMN_NAME, "float", 42.5));

                snapshot = new DatabaseSnapshot(database);
                Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
                assertNotNull(columnSnapshot);
                assertEquals(NEW_COLUMN_NAME.toUpperCase(), columnSnapshot.getName().toUpperCase());
                assertEquals(42.5, ((Number) columnSnapshot.getDefaultValue()).doubleValue());

                assertEquals(true, columnSnapshot.isNullable());

            }
        });
    }

    @Test
    public void execute_notNull() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));

                new JdbcTemplate(database).execute(new AddColumnStatement(null, TABLE_NAME, NEW_COLUMN_NAME, "int", 42, new NotNullConstraint()));

                snapshot = new DatabaseSnapshot(database);
                Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
                assertNotNull(columnSnapshot);
                assertEquals(false, columnSnapshot.isNullable());
            }
        });
    }

    @Test
    public void execute_primaryKey_nonAutoIncrement() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                AddColumnStatement statement = new AddColumnStatement(null, TABLE_NAME, NEW_COLUMN_NAME, "int", null, new PrimaryKeyConstraint());

                if (database instanceof CacheDatabase
                        || database instanceof H2Database
                        || database instanceof DB2Database
                        || database instanceof DerbyDatabase) {
                    try {
                        statement.getSqlStatement(database);
                        fail("Database does support adding primary keys, should throw exception");
                    } catch (StatementNotSupportedOnDatabaseException e) {
                        assertEquals("Adding primary key columns is not supported", e.getReason());
                    }
                    return;
                }
                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));

                new JdbcTemplate(database).execute(statement);

                snapshot = new DatabaseSnapshot(database);
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
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                DatabaseSnapshot snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
                assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));

                new JdbcTemplate(database).execute(new AddColumnStatement(TestContext.ALT_SCHEMA, TABLE_NAME, NEW_COLUMN_NAME, "varchar(50)", "new default"));

                snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
                Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
                assertNotNull(columnSnapshot);
                assertEquals(NEW_COLUMN_NAME.toUpperCase(), columnSnapshot.getName().toUpperCase());
                assertEquals("new default", columnSnapshot.getDefaultValue());

                assertEquals(true, columnSnapshot.isNullable());

            }
        });
    }

}
