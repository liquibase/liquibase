package liquibase.database.sql;

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
    private static final String FK_TABLE_NAME = "fk_table".toUpperCase();

    @Before
    @After
    public void dropTable() throws Exception {
        for (Database database : TestContext.getInstance().getAvailableDatabases()) {
            try {
                new JdbcTemplate(database).execute(new RawSqlStatement("drop table " + FK_TABLE_NAME));
            } catch (JDBCException e) {
                if (!database.getAutoCommitMode()) {
                    database.getConnection().rollback();
                }
            }
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
                        .addPrimaryKeyColumn("id", "int")
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

                assertNull(table.getColumn("name").getDefaultValue());
                assertTrue(table.getColumn("username").getDefaultValue().toString().indexOf("NEWUSER") >= 0);

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
                        .addPrimaryKeyColumn("id", "int")
                        .addColumn("name", "varchar(255)")
                        .addColumn("username", "varchar(255)", "'NEWUSER'");
                statement.addColumnConstraint(new AutoIncrementConstraint("id"));

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
                ForeignKeyConstraint fkConstraint = new ForeignKeyConstraint(foreignKeyName, TABLE_NAME + "(id)");
                CreateTableStatement statement = new CreateTableStatement(TABLE_NAME)
                        .addPrimaryKeyColumn("id", "int")
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
                ForeignKeyConstraint fkConstraint = new ForeignKeyConstraint(foreignKeyName, TABLE_NAME + "(id)");
                fkConstraint.setDeferrable(true);
                fkConstraint.setInitiallyDeferred(true);

                CreateTableStatement statement = new CreateTableStatement(TABLE_NAME)
                        .addPrimaryKeyColumn("id", "int")
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
    public void createTable_deleteCascadeForeignKeyColumn() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws JDBCException {
                CreateTableStatement statement = new CreateTableStatement(FK_TABLE_NAME)
                        .addPrimaryKeyColumn("id", "int")
                        .addColumn("name", "varchar(255)");

                new JdbcTemplate(database).execute(statement);

                String foreignKeyName = "fk_test_parent";
                ForeignKeyConstraint fkConstraint = new ForeignKeyConstraint(foreignKeyName, FK_TABLE_NAME + "(id)");
                fkConstraint.setDeleteCascade(true);

                statement = new CreateTableStatement(TABLE_NAME)
                        .addPrimaryKeyColumn("id", "int")
                        .addColumn("name", "varchar(255)")
                        .addColumn("parent_id", "int", fkConstraint);

                new JdbcTemplate(database).execute(statement);

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                Table table = snapshot.getTable(TABLE_NAME);
                assertEquals(TABLE_NAME.toUpperCase(), table.getName().toUpperCase());
                assertNotNull(table.getColumn("id"));

                ForeignKey foundForeignKey = snapshot.getForeignKey(foreignKeyName);
                assertNotNull(foundForeignKey);
                assertEquals(FK_TABLE_NAME, foundForeignKey.getPrimaryKeyTable().getName().toUpperCase());
                assertEquals("ID", foundForeignKey.getPrimaryKeyColumn().toUpperCase());
                assertEquals(TABLE_NAME, foundForeignKey.getForeignKeyTable().getName().toUpperCase());
                assertEquals("PARENT_ID", foundForeignKey.getForeignKeyColumn().toUpperCase());
//TODO: test when tested by diff                assertTrue(foundForeignKey.isDeleteCascade());
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
                        .addPrimaryKeyColumn("id", "int")
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

    @Test
    public void addPrimaryKeyColumn_oneColumn() {
        CreateTableStatement statement = new CreateTableStatement("tableName");
        statement.addPrimaryKeyColumn("id", "int");

        assertEquals(1, statement.getPrimaryKeyConstraint().getColumns().size());
    }

    @Test
    public void addPrimaryKeyColumn_multiColumn() {
        CreateTableStatement statement = new CreateTableStatement("tableName");
        statement.addPrimaryKeyColumn("id1", "int");
        statement.addPrimaryKeyColumn("id2", "int");

        assertEquals(2, statement.getPrimaryKeyConstraint().getColumns().size());
    }

    @Test
    public void addColumnConstraint_notNullConstraint() {
        CreateTableStatement statement = new CreateTableStatement("tableName");
        statement.addColumn("id", "int");

        assertFalse(statement.getNotNullColumns().contains("id"));

        statement.addColumnConstraint(new NotNullConstraint("id"));

        assertTrue(statement.getNotNullColumns().contains("id"));
    }

    @Test
    public void addColumnConstraint_ForeignKeyConstraint() {
        CreateTableStatement statement = new CreateTableStatement("tableName");
        statement.addColumn("id", "int");

        assertEquals(0, statement.getForeignKeyConstraints().size());

        statement.addColumnConstraint(new ForeignKeyConstraint("fk_test", "fkTable(id)").setColumn("id"));

        assertEquals(1, statement.getForeignKeyConstraints().size());
        assertEquals("fk_test", statement.getForeignKeyConstraints().iterator().next().getForeignKeyName());
    }

    @Test
    public void addColumnConstraint_UniqueConstraint() {
        CreateTableStatement statement = new CreateTableStatement("tableName");
        statement.addColumn("id", "int");

        assertEquals(0, statement.getUniqueConstraints().size());

        statement.addColumnConstraint(new UniqueConstraint("uq_test").addColumns("id"));

        assertEquals(1, statement.getUniqueConstraints().size());
        assertEquals("uq_test", statement.getUniqueConstraints().iterator().next().getConstraintName());
    }

    @Test
    public void createTable_tablespace() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws JDBCException {
                if (!database.supportsTablespaces()) {
                    return;
                }

                CreateTableStatement statement = new CreateTableStatement(TABLE_NAME)
                        .addPrimaryKeyColumn("id", "int")
                        .addColumn("name", "varchar(255)")
                        .addColumn("username", "varchar(255)", "'NEWUSER'")
                        .setTablespace("liquibase2");

                new JdbcTemplate(database).execute(statement);

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                Table table = snapshot.getTable(TABLE_NAME);
                assertEquals(TABLE_NAME.toUpperCase(), table.getName().toUpperCase());

                //todo: test that tablespace is correct when diff returns it
            }
        });
    }

    @Test
    public void getEndDelimiter() throws Exception {
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {

            public void performTest(Database database) throws JDBCException {

                CreateTableStatement statement = new CreateTableStatement("testTable");
                assertEquals(";", statement.getEndDelimiter(database));
            }
        });
    }
}