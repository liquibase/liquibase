package liquibase.database.statement;

import liquibase.database.Database;
import liquibase.database.HsqlDatabase;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.ForeignKey;
import liquibase.database.structure.Table;
import liquibase.database.template.JdbcTemplate;
import liquibase.exception.JDBCException;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Test;

public class CreateTableStatementTest extends AbstractSqlStatementTest {

    private static final String TABLE_NAME = "createTableStatementTest".toUpperCase();
    private static final String FK_TABLE_NAME = "fk_table".toUpperCase();

    protected void setupDatabase(Database database) throws Exception {
            dropTableIfExists(null, FK_TABLE_NAME, database);
            dropTableIfExists(null, TABLE_NAME, database);
            dropTableIfExists(TestContext.ALT_SCHEMA, TABLE_NAME, database);
    }

    protected SqlStatement generateTestStatement() {
        return new CreateTableStatement(null, null);
    }

    @Test
    public void createTable_standard() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new CreateTableStatement(null, TABLE_NAME)
                        .addPrimaryKeyColumn("id", "int", null, null)
                        .addColumn("name", "varchar(255)")
                        .addColumn("username", "varchar(255)", "'NEWUSER'")) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
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
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new CreateTableStatement(null, TABLE_NAME)
                        .addPrimaryKeyColumn("id", "int",null,  null)
                        .addColumn("name", "varchar(255)")
                        .addColumn("username", "varchar(255)", "'NEWUSER'")
                        .addColumnConstraint(new AutoIncrementConstraint("id"))) {

                    protected boolean supportsTest(Database database) {
                        return database.supportsAutoIncrement();
                    }

                    protected boolean expectedException(Database database, JDBCException exception) {
                        return !database.supportsAutoIncrement();
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
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
        final String foreignKeyName = "fk_test_parent";
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new CreateTableStatement(null, TABLE_NAME)
                        .addPrimaryKeyColumn("id", "int", null, null)
                        .addColumn("name", "varchar(255)")
                        .addColumn("parent_id", "int", new ForeignKeyConstraint(foreignKeyName, TABLE_NAME + "(id)"))) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        Table table = snapshot.getTable(TABLE_NAME);
                        assertEquals(TABLE_NAME.toUpperCase(), table.getName().toUpperCase());
                        assertNotNull(table.getColumn("id"));

                        ForeignKey foundForeignKey = snapshot.getForeignKey(foreignKeyName);
                        assertNotNull(foundForeignKey);
                        assertEquals(TABLE_NAME, foundForeignKey.getPrimaryKeyTable().getName().toUpperCase());
                        assertEquals("ID", foundForeignKey.getPrimaryKeyColumns().toUpperCase());
                        assertEquals(TABLE_NAME, foundForeignKey.getForeignKeyTable().getName().toUpperCase());
                        assertEquals("PARENT_ID", foundForeignKey.getForeignKeyColumns().toUpperCase());

                    }

                });
    }

    @Test
    public void createTable_deferrableForeignKeyColumn() throws Exception {
        final String foreignKeyName = "fk_test_parent";

        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new CreateTableStatement(null, TABLE_NAME)
                        .addPrimaryKeyColumn("id", "int", null, null)
                        .addColumn("name", "varchar(255)")
                        .addColumn("parent_id", "int",
                        new ForeignKeyConstraint(foreignKeyName, TABLE_NAME + "(id)")
                                .setDeferrable(true)
                                .setInitiallyDeferred(true))) {

                    protected boolean expectedException(Database database, JDBCException exception) {
                        return !database.supportsInitiallyDeferrableColumns();
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        Table table = snapshot.getTable(TABLE_NAME);
                        assertEquals(TABLE_NAME.toUpperCase(), table.getName().toUpperCase());
                        assertNotNull(table.getColumn("id"));

                        ForeignKey foundForeignKey = snapshot.getForeignKey(foreignKeyName);
                        assertNotNull(foundForeignKey);
                        assertEquals(TABLE_NAME, foundForeignKey.getPrimaryKeyTable().getName().toUpperCase());
                        assertEquals("ID", foundForeignKey.getPrimaryKeyColumns().toUpperCase());
                        assertEquals(TABLE_NAME, foundForeignKey.getForeignKeyTable().getName().toUpperCase());
                        assertEquals("PARENT_ID", foundForeignKey.getForeignKeyColumns().toUpperCase());
                        assertTrue(foundForeignKey.isDeferrable());
                        assertTrue(foundForeignKey.isInitiallyDeferred());
                    }

                });
    }

    @Test
    public void createTable_deleteCascadeForeignKeyColumn() throws Exception {
        final String foreignKeyName = "fk_test_parent";

        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new CreateTableStatement(null, TABLE_NAME)
                        .addPrimaryKeyColumn("id", "int", null, null)
                        .addColumn("name", "varchar(255)")
                        .addColumn("parent_id", "int", new ForeignKeyConstraint(foreignKeyName, FK_TABLE_NAME + "(id)").setDeleteCascade(true))) {

                    protected void setup(Database database) throws Exception {
                        new JdbcTemplate(database).execute(new CreateTableStatement(null, FK_TABLE_NAME)
                        .addPrimaryKeyColumn("id", "int",null,  null)
                        .addColumn("name", "varchar(255)"));
                        super.setup(database);
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        Table table = snapshot.getTable(TABLE_NAME);
                        assertEquals(TABLE_NAME.toUpperCase(), table.getName().toUpperCase());
                        assertNotNull(table.getColumn("id"));

                        ForeignKey foundForeignKey = snapshot.getForeignKey(foreignKeyName);
                        assertNotNull(foundForeignKey);
                        assertEquals(FK_TABLE_NAME, foundForeignKey.getPrimaryKeyTable().getName().toUpperCase());
                        assertEquals("ID", foundForeignKey.getPrimaryKeyColumns().toUpperCase());
                        assertEquals(TABLE_NAME, foundForeignKey.getForeignKeyTable().getName().toUpperCase());
                        assertEquals("PARENT_ID", foundForeignKey.getForeignKeyColumns().toUpperCase());
                        //TODO: test when tested by diff                assertTrue(foundForeignKey.isDeleteCascade());
                    }

                });
    }

    @Test
    public void createTable_uniqueColumn() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new CreateTableStatement(null, TABLE_NAME)
                        .addPrimaryKeyColumn("id", "int",null, null)
                        .addColumn("name", "varchar(255)")
                        .addColumn("username", "int", new UniqueConstraint("UQ_TESTCT_ID"), new NotNullConstraint())) {

                    protected boolean expectedException(Database database) {
                        return !(database instanceof HsqlDatabase);
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        Table table = snapshot.getTable(TABLE_NAME);
                        assertEquals(TABLE_NAME.toUpperCase(), table.getName().toUpperCase());
                        assertNotNull(table.getColumn("id"));

                        //todo: actually test for uniqueness when diff can check for it assertTrue(table.getColumn("username").isUnique());
                    }

                });
    }

    @Test
    public void addPrimaryKeyColumn_oneColumn() {
        CreateTableStatement statement = new CreateTableStatement(null, "tableName");
        statement.addPrimaryKeyColumn("id", "int", null, null);

        assertEquals(1, statement.getPrimaryKeyConstraint().getColumns().size());
    }

    @Test
    public void addPrimaryKeyColumn_multiColumn() {
        CreateTableStatement statement = new CreateTableStatement(null, "tableName");
        statement.addPrimaryKeyColumn("id1", "int", null, null);
        statement.addPrimaryKeyColumn("id2", "int", null, null);

        assertEquals(2, statement.getPrimaryKeyConstraint().getColumns().size());
    }

    @Test
    public void addColumnConstraint_notNullConstraint() {
        CreateTableStatement statement = new CreateTableStatement(null, "tableName");
        statement.addColumn("id", "int");

        assertFalse(statement.getNotNullColumns().contains("id"));

        statement.addColumnConstraint(new NotNullConstraint("id"));

        assertTrue(statement.getNotNullColumns().contains("id"));
    }

    @Test
    public void addColumnConstraint_ForeignKeyConstraint() {
        CreateTableStatement statement = new CreateTableStatement(null, "tableName");
        statement.addColumn("id", "int");

        assertEquals(0, statement.getForeignKeyConstraints().size());

        statement.addColumnConstraint(new ForeignKeyConstraint("fk_test", "fkTable(id)").setColumn("id"));

        assertEquals(1, statement.getForeignKeyConstraints().size());
        assertEquals("fk_test", statement.getForeignKeyConstraints().iterator().next().getForeignKeyName());
    }

    @Test
    public void addColumnConstraint_UniqueConstraint() {
        CreateTableStatement statement = new CreateTableStatement(null, "tableName");
        statement.addColumn("id", "int");

        assertEquals(0, statement.getUniqueConstraints().size());

        statement.addColumnConstraint(new UniqueConstraint("uq_test").addColumns("id"));

        assertEquals(1, statement.getUniqueConstraints().size());
        assertEquals("uq_test", statement.getUniqueConstraints().iterator().next().getConstraintName());
    }

    @Test
    public void createTable_tablespace() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new CreateTableStatement(null, TABLE_NAME)
                        .addPrimaryKeyColumn("id", "int", null, null)
                        .addColumn("name", "varchar(255)")
                        .addColumn("username", "varchar(255)", "'NEWUSER'")
                        .setTablespace("liquibase2")) {

                    protected boolean expectedException(Database database, JDBCException exception) {
                        return !database.supportsTablespaces();
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        Table table = snapshot.getTable(TABLE_NAME);
                        assertEquals(TABLE_NAME.toUpperCase(), table.getName().toUpperCase());

                        //todo: test that tablespace is correct when diff returns it
                    }
                });
    }

    @Test
    public void createTable_altSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                        .addPrimaryKeyColumn("id", "int", null, null)
                        .addColumn("name", "varchar(255)")
                        .addColumn("username", "varchar(255)", "'NEWUSER'")) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        Table table = snapshot.getTable(TABLE_NAME);
                        assertNotNull(table);
                        assertEquals(TABLE_NAME.toUpperCase(), table.getName().toUpperCase());
                    }
                });
    }
}
