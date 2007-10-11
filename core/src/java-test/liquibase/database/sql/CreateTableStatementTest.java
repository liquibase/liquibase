package liquibase.database.sql;

import liquibase.change.ConstraintsConfig;
import liquibase.database.Database;
import liquibase.database.HsqlDatabase;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.ForeignKey;
import liquibase.database.structure.Table;
import liquibase.database.template.JdbcTemplate;
import liquibase.exception.JDBCException;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.TestContext;
import liquibase.util.StringUtils;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class CreateTableStatementTest {

    private static final String TABLE_NAME = "createTableStatementTest".toUpperCase();

    @Before
    @After
    public void dropTable() throws Exception {
        for (Database database : TestContext.getInstance().getAvailableDatabases()) {
            try {
                new JdbcTemplate(database).execute(new RawSqlStatement("drop table " + TABLE_NAME));
            } catch (JDBCException e) {
                if (!database.getAutoCommitMode()) {
                    database.getConnection().rollback();
                }
            }
        }
    }

    @Test
    public void createTable_standard() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws JDBCException {
                CreateTableStatement statement = new CreateTableStatement(TABLE_NAME)
                        .addPrimaryKeyColumn("id", "int", false)
                        .addColumn("name", "varchar(255)")
                        .addColumn("username", "varchar(255)", "'NEWUSER'");

                new JdbcTemplate(database).execute(statement);

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                Table table = snapshot.getTable(TABLE_NAME);
                assertEquals(TABLE_NAME.toUpperCase(), table.getName().toUpperCase());
                assertNotNull(table.getColumn("id"));
                assertNotNull(table.getColumn("name"));
                assertNotNull(table.getColumn("username"));

                assertTrue(table.getColumn("id").isPrimaryKey());

                assertNull(StringUtils.trimToNull(table.getColumn("name").getDefaultValue()));
                assertTrue(table.getColumn("username").getDefaultValue().indexOf("NEWUSER") >= 0);

                assertFalse(table.getColumn("id").isAutoIncrement());
            }
        });
    }

    @Test
    public void createTable_autoincrementPK() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws JDBCException {
                if (!database.supportsAutoIncrement()) {
                    return;
                }

                CreateTableStatement statement = new CreateTableStatement(TABLE_NAME)
                        .addPrimaryKeyColumn("id", "int", true)
                        .addColumn("name", "varchar(255)")
                        .addColumn("username", "varchar(255)", "'NEWUSER'");

                new JdbcTemplate(database).execute(statement);

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                Table table = snapshot.getTable(TABLE_NAME);
                assertEquals(TABLE_NAME.toUpperCase(), table.getName().toUpperCase());
                assertNotNull(table.getColumn("id"));
                assertTrue(table.getColumn("id").isPrimaryKey());
                assertTrue(table.getColumn("id").isAutoIncrement());
            }
        });
    }

    @Test
    public void createTable_foreignKeyColumn() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws JDBCException {
                String foreignKeyName = "fk_test_parent";
                ForeignKeyConstraint fkConstraint = new ForeignKeyConstraint(foreignKeyName, TABLE_NAME+"(id)");
                CreateTableStatement statement = new CreateTableStatement(TABLE_NAME)
                        .addPrimaryKeyColumn("id", "int", false)
                        .addColumn("name", "varchar(255)")
                        .addColumn("parent_id", "int", fkConstraint);

                new JdbcTemplate(database).execute(statement);

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                Table table = snapshot.getTable(TABLE_NAME);
                assertEquals(TABLE_NAME.toUpperCase(), table.getName().toUpperCase());
                assertNotNull(table.getColumn("id"));

                ForeignKey foundForeignKey = snapshot.getForeignKey(foreignKeyName);
                assertNotNull(foundForeignKey);
                assertEquals(TABLE_NAME, foundForeignKey.getPrimaryKeyTable().getName().toUpperCase());
                assertEquals("ID", foundForeignKey.getPrimaryKeyColumn().toUpperCase());
                assertEquals(TABLE_NAME, foundForeignKey.getForeignKeyTable().getName().toUpperCase());
                assertEquals("PARENT_ID", foundForeignKey.getForeignKeyColumn().toUpperCase());
            }
        });
    }

    @Test
    public void createTable_deferrableForeignKeyColumn() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws JDBCException {
                if (!database.supportsInitiallyDeferrableColumns()) {
                    return;
                }

                String foreignKeyName = "fk_test_parent";
                ForeignKeyConstraint fkConstraint = new ForeignKeyConstraint(foreignKeyName, TABLE_NAME+"(id)");
                fkConstraint.setDeferrable(true);
                fkConstraint.setInitiallyDeferred(true);

                CreateTableStatement statement = new CreateTableStatement(TABLE_NAME)
                        .addPrimaryKeyColumn("id", "int", false)
                        .addColumn("name", "varchar(255)")
                        .addColumn("parent_id", "int", fkConstraint);

                new JdbcTemplate(database).execute(statement);

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                Table table = snapshot.getTable(TABLE_NAME);
                assertEquals(TABLE_NAME.toUpperCase(), table.getName().toUpperCase());
                assertNotNull(table.getColumn("id"));

                ForeignKey foundForeignKey = snapshot.getForeignKey(foreignKeyName);
                assertNotNull(foundForeignKey);
                assertEquals(TABLE_NAME, foundForeignKey.getPrimaryKeyTable().getName().toUpperCase());
                assertEquals("ID", foundForeignKey.getPrimaryKeyColumn().toUpperCase());
                assertEquals(TABLE_NAME, foundForeignKey.getForeignKeyTable().getName().toUpperCase());
                assertEquals("PARENT_ID", foundForeignKey.getForeignKeyColumn().toUpperCase());
                assertTrue(foundForeignKey.isDeferrable());
                assertTrue(foundForeignKey.isInitiallyDeferred());
            }
        });
    }

    @Test
    public void createTable_uniqueColumn() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws JDBCException {
                if (database instanceof HsqlDatabase) {
                    return;
                }

                UniqueConstraint uniqueConstraint = new UniqueConstraint("UQ_TESTCT_ID");
                NotNullConstraint notNullConstraint = new NotNullConstraint();
                CreateTableStatement statement = new CreateTableStatement(TABLE_NAME)
                        .addPrimaryKeyColumn("id", "int", false)
                        .addColumn("name", "varchar(255)")
                        .addColumn("username", "int", uniqueConstraint, notNullConstraint);

                new JdbcTemplate(database).execute(statement);

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                Table table = snapshot.getTable(TABLE_NAME);
                assertEquals(TABLE_NAME.toUpperCase(), table.getName().toUpperCase());
                assertNotNull(table.getColumn("id"));

                //todo: actually test for uniqueness when diff can check for it assertTrue(table.getColumn("username").isUnique());
            }
        });
    }


}