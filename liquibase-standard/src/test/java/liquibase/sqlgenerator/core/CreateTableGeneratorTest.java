package liquibase.sqlgenerator.core;

import java.math.BigInteger;

import liquibase.database.MockDatabaseConnection;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.DatabaseException;
import liquibase.statement.*;
import liquibase.structure.core.Table;
import org.junit.Test;

import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.database.core.AbstractDb2Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.core.CockroachDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.core.IntType;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.statement.core.CreateTableStatement;
import liquibase.test.TestContext;

import static org.junit.Assert.*;

public class CreateTableGeneratorTest extends AbstractSqlGeneratorTest<CreateTableStatement> {

    protected static final String TABLE_NAME = "TABLE_NAME";
    protected static final String CATALOG_NAME = "CATALOG_NAME";
    protected static final String SCHEMA_NAME = "SCHEMA_NAME";

    protected static final String COLUMN_NAME1 = "COLUMN1_NAME";

    public CreateTableGeneratorTest() throws Exception {
        super(new CreateTableGenerator());
    }

    @Override
    protected CreateTableStatement createSampleSqlStatement() {
        CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
        statement.addColumn(COLUMN_NAME1, new IntType());
        return statement;
    }

    @Test
    public void testWithColumnWithDefaultValue() {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof OracleDatabase) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(COLUMN_NAME1, DataTypeFactory.getInstance().fromDescription("java.sql.Types.TIMESTAMP", database), new ColumnConfig().setDefaultValue("null").getDefaultValueObject());
                if (shouldBeImplementation(database)) {
                    assertEquals("CREATE TABLE CATALOG_NAME.TABLE_NAME (COLUMN1_NAME TIMESTAMP DEFAULT null)", this.generatorUnderTest.generateSql(statement, database, null)[0].toSql());
                }
            }
        }
    }

    @Test
    public void testWithColumnSpecificIntType() {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(COLUMN_NAME1, DataTypeFactory.getInstance().fromDescription("int(11) unsigned", database));
        }
    }

    @Test
    public void testWithDeferredPKs() {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
            statement.addColumn(COLUMN_NAME1, DataTypeFactory.getInstance().fromDescription("int", database),
                    new PrimaryKeyConstraint().addColumns(COLUMN_NAME1).setDeferrable(true).setInitiallyDeferred(true));

            if (database.supportsInitiallyDeferrableColumns() && !(database instanceof SybaseASADatabase)) {
                assertTrue(this.generatorUnderTest.generateSql(statement, database, null)[0].toSql().contains("DEFERRABLE"));
            } else {
                assertFalse(this.generatorUnderTest.generateSql(statement, database, null)[0].toSql().contains("DEFERRABLE"));
            }
        }
    }

    //    @Test
//    public void createTable_standard() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new CreateTableStatement(null, TABLE_NAME)
//                        .addPrimaryKeyColumn("id", "int", null, null)
//                        .addColumn("name", "varchar(255)")
//                        .addColumn("username", "varchar(255)", "'NEWUSER'")) {
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        Table table = snapshot.getTable(TABLE_NAME);
//                        assertEquals(TABLE_NAME.toUpperCase(), table.getName().toUpperCase());
//                        assertNotNull(table.getColumn("id"));
//                        assertNotNull(table.getColumn("name"));
//                        assertNotNull(table.getColumn("username"));
//
//                        assertTrue(table.getColumn("id").isPrimaryKey());
//
//                        assertNull(table.getColumn("name").getDefaultValue());
//                        assertTrue(table.getColumn("username").getDefaultValue().toString().indexOf("NEWUSER") >= 0);
//
//                        assertFalse(table.getColumn("id").isAutoIncrement());
//                    }
//
//                });
//    }
//
//    @Test
//    public void createTable_autoincrementPK() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new CreateTableStatement(null, TABLE_NAME)
//                        .addPrimaryKeyColumn("id", "int",null,  null)
//                        .addColumn("name", "varchar(255)")
//                        .addColumn("username", "varchar(255)", "'NEWUSER'")
//                        .addColumnConstraint(new AutoIncrementConstraint("id"))) {
//
//                    protected boolean supportsTest(Database database) {
//                        return database.supportsAutoIncrement();
//                    }
//
//                    protected boolean expectedException(Database database, DatabaseException exception) {
//                        return !database.supportsAutoIncrement();
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        Table table = snapshot.getTable(TABLE_NAME);
//                        assertEquals(TABLE_NAME.toUpperCase(), table.getName().toUpperCase());
//                        assertNotNull(table.getColumn("id"));
//                        assertTrue(table.getColumn("id").isPrimaryKey());
//                        assertTrue(table.getColumn("id").isAutoIncrement());
//                    }
//                });
//    }
//
//    @Test
//    public void createTable_foreignKeyColumn() throws Exception {
//        final String foreignKeyName = "fk_test_parent";
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new CreateTableStatement(null, TABLE_NAME)
//                        .addPrimaryKeyColumn("id", "int", null, null)
//                        .addColumn("name", "varchar(255)")
//                        .addColumn("parent_id", "int", new ForeignKeyConstraint(foreignKeyName, TABLE_NAME + "(id)"))) {
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        Table table = snapshot.getTable(TABLE_NAME);
//                        assertEquals(TABLE_NAME.toUpperCase(), table.getName().toUpperCase());
//                        assertNotNull(table.getColumn("id"));
//
//                        ForeignKey foundForeignKey = snapshot.getForeignKey(foreignKeyName);
//                        assertNotNull(foundForeignKey);
//                        assertEquals(TABLE_NAME, foundForeignKey.getPrimaryKeyTable().getName().toUpperCase());
//                        assertEquals("ID", foundForeignKey.getPrimaryKeyColumns().toUpperCase());
//                        assertEquals(TABLE_NAME, foundForeignKey.getForeignKeyTable().getName().toUpperCase());
//                        assertEquals("PARENT_ID", foundForeignKey.getForeignKeyColumns().toUpperCase());
//
//                    }
//
//                });
//    }
//
//    @Test
//    public void createTable_deferrableForeignKeyColumn() throws Exception {
//        final String foreignKeyName = "fk_test_parent";
//
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new CreateTableStatement(null, TABLE_NAME)
//                        .addPrimaryKeyColumn("id", "int", null, null)
//                        .addColumn("name", "varchar(255)")
//                        .addColumn("parent_id", "int",
//                        new ForeignKeyConstraint(foreignKeyName, TABLE_NAME + "(id)")
//                                .setDeferrable(true)
//                                .setInitiallyDeferred(true))) {
//
//                    protected boolean expectedException(Database database, DatabaseException exception) {
//                        return !database.supportsInitiallyDeferrableColumns();
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        Table table = snapshot.getTable(TABLE_NAME);
//                        assertEquals(TABLE_NAME.toUpperCase(), table.getName().toUpperCase());
//                        assertNotNull(table.getColumn("id"));
//
//                        ForeignKey foundForeignKey = snapshot.getForeignKey(foreignKeyName);
//                        assertNotNull(foundForeignKey);
//                        assertEquals(TABLE_NAME, foundForeignKey.getPrimaryKeyTable().getName().toUpperCase());
//                        assertEquals("ID", foundForeignKey.getPrimaryKeyColumns().toUpperCase());
//                        assertEquals(TABLE_NAME, foundForeignKey.getForeignKeyTable().getName().toUpperCase());
//                        assertEquals("PARENT_ID", foundForeignKey.getForeignKeyColumns().toUpperCase());
//                        assertTrue(foundForeignKey.isDeferrable());
//                        assertTrue(foundForeignKey.isInitiallyDeferred());
//                    }
//
//                });
//    }
//
//    @Test
//    public void createTable_deleteCascadeForeignKeyColumn() throws Exception {
//        final String foreignKeyName = "fk_test_parent";
//
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new CreateTableStatement(null, TABLE_NAME)
//                        .addPrimaryKeyColumn("id", "int", null, null)
//                        .addColumn("name", "varchar(255)")
//                        .addColumn("parent_id", "int", new ForeignKeyConstraint(foreignKeyName, FK_TABLE_NAME + "(id)").setDeleteCascade(true))) {
//
//                    protected void setup(Database database) throws Exception {
//                        new Executor(database).execute(new CreateTableStatement(null, FK_TABLE_NAME)
//                        .addPrimaryKeyColumn("id", "int",null,  null)
//                        .addColumn("name", "varchar(255)"));
//                        super.setup(database);
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        Table table = snapshot.getTable(TABLE_NAME);
//                        assertEquals(TABLE_NAME.toUpperCase(), table.getName().toUpperCase());
//                        assertNotNull(table.getColumn("id"));
//
//                        ForeignKey foundForeignKey = snapshot.getForeignKey(foreignKeyName);
//                        assertNotNull(foundForeignKey);
//                        assertEquals(FK_TABLE_NAME, foundForeignKey.getPrimaryKeyTable().getName().toUpperCase());
//                        assertEquals("ID", foundForeignKey.getPrimaryKeyColumns().toUpperCase());
//                        assertEquals(TABLE_NAME, foundForeignKey.getForeignKeyTable().getName().toUpperCase());
//                        assertEquals("PARENT_ID", foundForeignKey.getForeignKeyColumns().toUpperCase());
//                        //TODO: test when tested by diff                assertTrue(foundForeignKey.isDeleteCascade());
//                    }
//
//                });
//    }
//
//    @Test
//    public void createTable_uniqueColumn() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new CreateTableStatement(null, TABLE_NAME)
//                        .addPrimaryKeyColumn("id", "int",null, null)
//                        .addColumn("name", "varchar(255)")
//                        .addColumn("username", "int", new UniqueConstraint("UQ_TESTCT_ID"), new NotNullConstraint())) {
//
//                    protected boolean expectedException(Database database) {
//                        return !(database instanceof HsqlDatabase) || database  instanceof H2Database;
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        Table table = snapshot.getTable(TABLE_NAME);
//                        assertEquals(TABLE_NAME.toUpperCase(), table.getName().toUpperCase());
//                        assertNotNull(table.getColumn("id"));
//
//                        //todo: actually test for uniqueness when diff can check for it assertTrue(table.getColumn("username").isUnique());
//                    }
//
//                });
//    }
//
//    @Test
//    public void addPrimaryKeyColumn_oneColumn() {
//        CreateTableStatement statement = new CreateTableStatement(null, "tableName");
//        statement.addPrimaryKeyColumn("id", "int", null, null);
//
//        assertEquals(1, statement.getPrimaryKeyConstraint().getColumns().size());
//    }
//
//    @Test
//    public void addPrimaryKeyColumn_multiColumn() {
//        CreateTableStatement statement = new CreateTableStatement(null, "tableName");
//        statement.addPrimaryKeyColumn("id1", "int", null, null);
//        statement.addPrimaryKeyColumn("id2", "int", null, null);
//
//        assertEquals(2, statement.getPrimaryKeyConstraint().getColumns().size());
//    }
//
//    @Test
//    public void addColumnConstraint_notNullConstraint() {
//        CreateTableStatement statement = new CreateTableStatement(null, "tableName");
//        statement.addColumn("id", "int");
//
//        assertFalse(statement.getNotNullColumns().contains("id"));
//
//        statement.addColumnConstraint(new NotNullConstraint("id"));
//
//        assertTrue(statement.getNotNullColumns().contains("id"));
//    }
//
//    @Test
//    public void addColumnConstraint_ForeignKeyConstraint() {
//        CreateTableStatement statement = new CreateTableStatement(null, "tableName");
//        statement.addColumn("id", "int");
//
//        assertEquals(0, statement.getForeignKeyConstraints().size());
//
//        statement.addColumnConstraint(new ForeignKeyConstraint("fk_test", "fkTable(id)").setColumns("id"));
//
//        assertEquals(1, statement.getForeignKeyConstraints().size());
//        assertEquals("fk_test", statement.getForeignKeyConstraints().iterator().next().getForeignKeyName());
//    }
//
//    @Test
//    public void addColumnConstraint_UniqueConstraint() {
//        CreateTableStatement statement = new CreateTableStatement(null, "tableName");
//        statement.addColumn("id", "int");
//
//        assertEquals(0, statement.getUniqueConstraints().size());
//
//        statement.addColumnConstraint(new UniqueConstraint("uq_test").addColumns("id"));
//
//        assertEquals(1, statement.getUniqueConstraints().size());
//        assertEquals("uq_test", statement.getUniqueConstraints().iterator().next().getConstraintName());
//    }
//
//    @Test
//    public void createTable_tablespace() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new CreateTableStatement(null, TABLE_NAME)
//                        .addPrimaryKeyColumn("id", "int", null, null)
//                        .addColumn("name", "varchar(255)")
//                        .addColumn("username", "varchar(255)", "'NEWUSER'")
//                        .setTablespace("liquibase2")) {
//
//                    protected boolean expectedException(Database database, DatabaseException exception) {
//                        return !database.supportsTablespaces();
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        Table table = snapshot.getTable(TABLE_NAME);
//                        assertEquals(TABLE_NAME.toUpperCase(), table.getName().toUpperCase());
//
//                        //todo: test that tablespace is correct when diff returns it
//                    }
//                });
//    }
//
//    @Test
//    public void createTable_altSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
//                        .addPrimaryKeyColumn("id", "int", null, null)
//                        .addColumn("name", "varchar(255)")
//                        .addColumn("username", "varchar(255)", "'NEWUSER'")) {
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        Table table = snapshot.getTable(TABLE_NAME);
//                        assertNotNull(table);
//                        assertEquals(TABLE_NAME.toUpperCase(), table.getName().toUpperCase());
//                    }
//                });
//    }

    @Test
    public void testDefaultValueCurrentTimestampDB2Database() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof AbstractDb2Database) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("datetime", database),
                    new DatabaseFunction("current_timestamp")
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("CREATE TABLE \"CATALOG_NAME\".TABLE_NAME (COLUMN1_NAME TIMESTAMP DEFAULT CURRENT TIMESTAMP)", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementDB2Database() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof AbstractDb2Database) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("CREATE TABLE \"CATALOG_NAME\".TABLE_NAME (COLUMN1_NAME BIGINT GENERATED BY DEFAULT AS IDENTITY)", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementStartWithDB2Database() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof AbstractDb2Database) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1, BigInteger.ZERO, null)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("CREATE TABLE \"CATALOG_NAME\".TABLE_NAME (COLUMN1_NAME BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 0))", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementStartWithIncrementByDB2Database() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof AbstractDb2Database) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1, BigInteger.ZERO, BigInteger.TEN)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("CREATE TABLE \"CATALOG_NAME\".TABLE_NAME (COLUMN1_NAME BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 0, INCREMENT BY 10))", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementDerbyDatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof DerbyDatabase) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("CREATE TABLE CATALOG_NAME.TABLE_NAME (COLUMN1_NAME BIGINT GENERATED BY DEFAULT AS IDENTITY)", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementStartWithDerbyDatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof DerbyDatabase) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1, BigInteger.ZERO, null)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("CREATE TABLE CATALOG_NAME.TABLE_NAME (COLUMN1_NAME BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 0))", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementStartWithIncrementByDerbyDatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof DerbyDatabase) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1, BigInteger.ZERO, BigInteger.TEN)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("CREATE TABLE CATALOG_NAME.TABLE_NAME (COLUMN1_NAME BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 0, INCREMENT BY 10))", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementH2Database() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof H2Database) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("CREATE TABLE SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME BIGINT GENERATED BY DEFAULT AS IDENTITY)", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementStartWithH2Database() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof H2Database) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1, BigInteger.ZERO, null)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("CREATE TABLE SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 0))", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementStartWithIncrementByH2Database() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof H2Database) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1, BigInteger.ZERO, BigInteger.TEN)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("CREATE TABLE SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 0 INCREMENT BY 10))", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementHsqlDatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof HsqlDatabase) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("CREATE TABLE SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME BIGINT GENERATED BY DEFAULT AS IDENTITY)", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementStartWithHsqlDatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof HsqlDatabase) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1, BigInteger.ONE, null)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("CREATE TABLE SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 1))", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementStartWithIncrementByHsqlDatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof HsqlDatabase) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1, BigInteger.ONE, BigInteger.TEN)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("CREATE TABLE SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 1 INCREMENT BY 10))", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementMSSQLDatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof MSSQLDatabase) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new ColumnConstraint[]{
                        new AutoIncrementConstraint(COLUMN_NAME1),
                        new NotNullConstraint(COLUMN_NAME1)}
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("Error on " + database, "CREATE TABLE CATALOG_NAME.SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME " +
                    "bigint IDENTITY (1, 1) NOT NULL)", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementStartWithMSSQLDatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof MSSQLDatabase) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new ColumnConstraint[]{
                        new AutoIncrementConstraint(COLUMN_NAME1, BigInteger.ZERO, null),
                        new NotNullConstraint(COLUMN_NAME1)
                    }
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("CREATE TABLE CATALOG_NAME.SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME bigint IDENTITY (0, 1) " +
                    "NOT NULL)", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementStartWithIncrementByMSSQLDatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof MSSQLDatabase) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new ColumnConstraint[]{
                        new AutoIncrementConstraint(COLUMN_NAME1, BigInteger.ZERO, BigInteger.TEN),
                        new NotNullConstraint(COLUMN_NAME1)
                    }
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("CREATE TABLE CATALOG_NAME.SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME bigint IDENTITY (0, 10) NOT NULL)", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementMySQLDatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof MySQLDatabase) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("CREATE TABLE CATALOG_NAME.TABLE_NAME (COLUMN1_NAME BIGINT AUTO_INCREMENT NULL)", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementStartWithMySQLDatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof MySQLDatabase) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1, BigInteger.valueOf(2), null)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("CREATE TABLE CATALOG_NAME.TABLE_NAME (COLUMN1_NAME BIGINT AUTO_INCREMENT NULL) AUTO_INCREMENT=2", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementStartWithIncrementByMySQLDatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof MySQLDatabase) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1, BigInteger.valueOf(2), BigInteger.TEN)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                // increment by not supported by MySQL
                assertEquals("CREATE TABLE CATALOG_NAME.TABLE_NAME (COLUMN1_NAME BIGINT AUTO_INCREMENT NULL) AUTO_INCREMENT=2", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementPostgresDatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database.getClass().equals(PostgresDatabase.class)) {
                MockDatabaseConnection conn = new MockDatabaseConnection();
                int saveMajorVersion = getDatabaseMajorVersion(conn);
                conn.setDatabaseMajorVersion(9);
                database.setConnection(conn);
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("CREATE TABLE SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME BIGSERIAL)", generatedSql[0].toSql());

                conn.setDatabaseMajorVersion(10);
                generatedSql = this.generatorUnderTest.generateSql(statement, database, null);
                assertEquals("CREATE TABLE SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME BIGINT GENERATED BY DEFAULT AS IDENTITY)", generatedSql[0].toSql());

                conn.setDatabaseMajorVersion(saveMajorVersion);
            }
        }
    }

    @Test
    public void testAutoIncrementCockroachDatabase() {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database.getClass().equals(CockroachDatabase.class)) {
                MockDatabaseConnection conn = new MockDatabaseConnection();
                int saveMajorVersion = getDatabaseMajorVersion(conn);
                conn.setDatabaseMajorVersion(9);
                database.setConnection(conn);
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addPrimaryKeyColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    null,
                    "",
                    null,
                    new AutoIncrementConstraint(COLUMN_NAME1)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("CREATE TABLE SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME BIGSERIAL NOT NULL, CONSTRAINT \"TABLE_NAME_pkey\" PRIMARY KEY (COLUMN1_NAME))", generatedSql[0].toSql());

                conn.setDatabaseMajorVersion(13);
                generatedSql = this.generatorUnderTest.generateSql(statement, database, null);
                assertEquals("CREATE TABLE SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME BIGSERIAL NOT NULL, CONSTRAINT \"TABLE_NAME_pkey\" PRIMARY KEY (COLUMN1_NAME))", generatedSql[0].toSql());
                conn.setDatabaseMajorVersion(saveMajorVersion);
            }
        }
    }

    private int getDatabaseMajorVersion(MockDatabaseConnection conn) {
        try {
            return conn.getDatabaseMajorVersion();
        } catch (DatabaseException dbe) {
            return 999;
        }
    }

    @Test
    public void testAutoIncrementGenerationTypePostgresDatabase() {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database.getClass().equals(PostgresDatabase.class)) {
                MockDatabaseConnection conn = new MockDatabaseConnection();
                int saveMajorVersion = getDatabaseMajorVersion(conn);
                conn.setDatabaseMajorVersion(9);
                database.setConnection(conn);
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                AutoIncrementConstraint constraint = new AutoIncrementConstraint(COLUMN_NAME1);
                constraint.setGenerationType("ALWAYS");
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    constraint
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("CREATE TABLE SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME BIGSERIAL)", generatedSql[0].toSql());

                conn.setDatabaseMajorVersion(10);
                generatedSql = this.generatorUnderTest.generateSql(statement, database, null);
                assertEquals("CREATE TABLE SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME BIGINT GENERATED ALWAYS AS IDENTITY)", generatedSql[0].toSql());
                conn.setDatabaseMajorVersion(saveMajorVersion);
            }
        }
    }

    @Test
    public void testAutoIncrementStartWithPostgresDatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof PostgresDatabase) {
                MockDatabaseConnection conn = new MockDatabaseConnection();
                int saveMajorVersion = getDatabaseMajorVersion(conn);
                conn.setDatabaseMajorVersion(9);
                database.setConnection(conn);
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1, BigInteger.ZERO, null)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                // start with supported over generated sequence
                assertEquals("CREATE TABLE SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME BIGSERIAL)", generatedSql[0].toSql());
                conn.setDatabaseMajorVersion(10);
                generatedSql = this.generatorUnderTest.generateSql(statement, database, null);
                conn.setDatabaseMajorVersion(saveMajorVersion);
            }
        }
    }

    @Test
    public void testAutoIncrementStartWithIncrementByPostgresDatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database.getClass().equals(PostgresDatabase.class)) {
                MockDatabaseConnection conn = new MockDatabaseConnection();
                int saveMajorVersion = getDatabaseMajorVersion(conn);
                conn.setDatabaseMajorVersion(9);
                database.setConnection(conn);
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1, BigInteger.ZERO, BigInteger.TEN)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                // start with and increment by supported over generated sequence
                assertEquals("CREATE TABLE SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME BIGSERIAL)", generatedSql[0].toSql());
                conn.setDatabaseMajorVersion(10);
                generatedSql = this.generatorUnderTest.generateSql(statement, database, null);
                assertEquals("CREATE TABLE SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 0 INCREMENT BY 10))", generatedSql[0].toSql());
                conn.setDatabaseMajorVersion(saveMajorVersion);
            }
        }
    }

    @Test
    public void testAutoIncrementSQLiteDatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof SQLiteDatabase) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("CREATE TABLE SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME BIGINT AUTOINCREMENT)", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementStartWithSQLiteDatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof SQLiteDatabase) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1, BigInteger.valueOf(2), null)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                // start with not supported by SQLlite
                assertEquals("CREATE TABLE SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME BIGINT AUTOINCREMENT)", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementStartWithIncrementBySQLiteDatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof SQLiteDatabase) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1, BigInteger.valueOf(2), BigInteger.TEN)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                // start with and increment by not supported by SQLite
                assertEquals("CREATE TABLE SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME BIGINT AUTOINCREMENT)", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementSybaseASADatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof SybaseASADatabase) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("CREATE TABLE SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME BIGINT DEFAULT AUTOINCREMENT NULL)", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementStartWithSybaseASADatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof SybaseASADatabase) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1, BigInteger.valueOf(2), null)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                // start with not supported by SybaseASA
                assertEquals("CREATE TABLE SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME BIGINT DEFAULT AUTOINCREMENT NULL)", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementStartWithIncrementBySybaseASADatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof SybaseASADatabase) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1, BigInteger.valueOf(2), BigInteger.TEN)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                // start with and increment by not supported by SybaseASA
                assertEquals("CREATE TABLE SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME BIGINT DEFAULT AUTOINCREMENT NULL)", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementSybaseDatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof SybaseDatabase) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("Error with "+database, "CREATE TABLE SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME BIGINT IDENTITY NULL)", generatedSql[0].toSql());
            }
        }
    }

	@Test
	public void testAutoIncrementSybaseDatabaseWithSpecialCharacters() throws Exception {
		for (Database database : TestContext.getInstance().getAllDatabases()) {
			if (database instanceof SybaseDatabase) {
				CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, "SCHEMA-NAME", "TABLE NAME");
				statement.addColumn(
						"1ST_COLUMN_NAME",
						DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
						new AutoIncrementConstraint("1ST_COLUMN_NAME")
				);

				Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

				assertEquals("Error with "+database, "CREATE TABLE [SCHEMA-NAME].[TABLE NAME] ([1ST_COLUMN_NAME] BIGINT IDENTITY NULL)", generatedSql[0].toSql());
			}
		}
	}

    @Test
    public void testAutoIncrementStartWithSybaseDatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof SybaseDatabase) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1, BigInteger.valueOf(2), null)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                // start with not supported by Sybase
                assertEquals("CREATE TABLE SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME BIGINT IDENTITY NULL)", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementStartWithIncrementBySybaseDatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof SybaseDatabase) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("BIGINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1, BigInteger.valueOf(2), BigInteger.TEN)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                // start with and increment by not supported by Sybase
                assertEquals("CREATE TABLE SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME BIGINT IDENTITY NULL)", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementOracleDatabase() {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof OracleDatabase) {
                MockDatabaseConnection conn = new MockDatabaseConnection();
                database.setConnection(conn);
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("SMALLINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);
                assertEquals("CREATE TABLE CATALOG_NAME.TABLE_NAME (COLUMN1_NAME NUMBER(5) GENERATED BY DEFAULT AS IDENTITY)",
                    generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementGenerationTypeOracleDatabase() {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof OracleDatabase) {
                MockDatabaseConnection conn = new MockDatabaseConnection();
                database.setConnection(conn);
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                AutoIncrementConstraint autoIncrementConstraint = new AutoIncrementConstraint(COLUMN_NAME1);
                autoIncrementConstraint.setGenerationType("ALWAYS");
                autoIncrementConstraint.setDefaultOnNull(true);    // ignore when ALWAYS
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("SMALLINT{autoIncrement:true}", database),
                    autoIncrementConstraint
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);
                assertEquals("CREATE TABLE CATALOG_NAME.TABLE_NAME (COLUMN1_NAME NUMBER(5) GENERATED ALWAYS AS IDENTITY)",
                    generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementGenerationTypeDefaultOnNullOracleDatabase() {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof OracleDatabase) {
                MockDatabaseConnection conn = new MockDatabaseConnection();
                database.setConnection(conn);
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                AutoIncrementConstraint autoIncrementConstraint = new AutoIncrementConstraint(COLUMN_NAME1);
                autoIncrementConstraint.setGenerationType("BY DEFAULT");
                autoIncrementConstraint.setDefaultOnNull(true);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("SMALLINT{autoIncrement:true}", database),
                    autoIncrementConstraint
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);
                assertEquals("CREATE TABLE CATALOG_NAME.TABLE_NAME (COLUMN1_NAME NUMBER(5) GENERATED BY DEFAULT ON NULL AS IDENTITY)",
                    generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementStartWithOracleDatabase() {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof OracleDatabase) {
                MockDatabaseConnection conn = new MockDatabaseConnection();
                database.setConnection(conn);
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("SMALLINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1, BigInteger.ZERO, null)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);
                assertEquals("CREATE TABLE CATALOG_NAME.TABLE_NAME (COLUMN1_NAME NUMBER(5) GENERATED BY DEFAULT AS IDENTITY (START WITH 0))",
                    generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAutoIncrementStartWithIncrementByOracleDatabase() {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof OracleDatabase) {
                MockDatabaseConnection conn = new MockDatabaseConnection();
                database.setConnection(conn);
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                    COLUMN_NAME1,
                    DataTypeFactory.getInstance().fromDescription("SMALLINT{autoIncrement:true}", database),
                    new AutoIncrementConstraint(COLUMN_NAME1, BigInteger.ZERO, BigInteger.TEN)
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);
                assertEquals("CREATE TABLE CATALOG_NAME.TABLE_NAME (COLUMN1_NAME NUMBER(5) GENERATED BY DEFAULT AS IDENTITY (START WITH 0, INCREMENT BY 10))",
                    generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void createReferencesSchemaEscaped() throws Exception {
        Database database = new PostgresDatabase();
        database.setOutputDefaultSchema(true);
        database.setDefaultSchemaName("my-schema");
        CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
        statement.addColumnConstraint(new ForeignKeyConstraint("fk_test_parent", TABLE_NAME + "(id)").setColumn("id"));
        Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);
        assertEquals("CREATE TABLE SCHEMA_NAME.TABLE_NAME (, CONSTRAINT fk_test_parent FOREIGN KEY (id) REFERENCES \"my-schema\".TABLE_NAME(id))", generatedSql[0].toSql());
    }

    @Test
    public void combineUniqueConstraints() {
        Database database = new SQLiteDatabase();
        database.setOutputDefaultSchema(true);
        LiquibaseDataType integerType = DataTypeFactory.getInstance().fromDescription("INTEGER", database);

        CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
        statement.addColumn("MY_KEY", integerType, new UniqueConstraint("SAME"));
        statement.addColumn("MY_OTHER_KEY", integerType, new UniqueConstraint("SAME"));
        statement.addColumn("SINGLE_UNIQUE_KEY", integerType, new UniqueConstraint("DIFFERENT"));
        statement.addColumn("UNIQUE_NO_CONSTRAINT_NAME", integerType, new UniqueConstraint());
        Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);
        String expectedSql = "CREATE TABLE CATALOG_NAME.TABLE_NAME " +
                "(MY_KEY INTEGER, MY_OTHER_KEY INTEGER, " +
                "SINGLE_UNIQUE_KEY INTEGER, UNIQUE_NO_CONSTRAINT_NAME INTEGER, " +
                "UNIQUE (UNIQUE_NO_CONSTRAINT_NAME), " +
                "CONSTRAINT SAME UNIQUE (MY_KEY, MY_OTHER_KEY), " +
                "CONSTRAINT DIFFERENT UNIQUE (SINGLE_UNIQUE_KEY))";
        assertEquals(expectedSql, generatedSql[0].toSql());
    }
    @Test
    public void testGeneratedAlwaysPostgresDatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof PostgresDatabase) {
                MockDatabaseConnection conn = new MockDatabaseConnection();
                int saveMajorVersion = getDatabaseMajorVersion(conn);
                conn.setDatabaseMajorVersion(9);
                database.setConnection(conn);
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addColumn(
                        COLUMN_NAME1,
                        DataTypeFactory.getInstance().fromDescription("int", database),
                        new ColumnConfig().setDefaultValue("GENERATED ALWAYS AS (rank_1 / 2) STORED").getDefaultValueObject()
                );

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("CREATE TABLE SCHEMA_NAME.TABLE_NAME (COLUMN1_NAME INTEGER GENERATED ALWAYS AS (rank_1 / 2) STORED)", generatedSql[0].toSql());
                conn.setDatabaseMajorVersion(saveMajorVersion);
            }
        }
    }

    @Test
    public void testWithEmptyPrimaryKeyTablespaceOracleDatabase() {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof OracleDatabase) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
                statement.addPrimaryKeyColumn(COLUMN_NAME1, DataTypeFactory.getInstance().fromDescription("varchar2(40)", database), new ColumnConfig().setDefaultValue(null).getDefaultValueObject(), "PK", "");
                if (shouldBeImplementation(database)) {
                    assertEquals("CREATE TABLE CATALOG_NAME.TABLE_NAME (COLUMN1_NAME VARCHAR2(40) NOT NULL, CONSTRAINT PK PRIMARY KEY (COLUMN1_NAME))", this.generatorUnderTest.generateSql(statement, database, null)[0].toSql());
                }
            }
        }
    }

    @Test
    public void testWithCreateIfNotExists() {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database.supportsCreateIfNotExists(Table.class)) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME, true);
                statement.addColumn(COLUMN_NAME1, DataTypeFactory.getInstance().fromDescription("java.sql.Types.TIMESTAMP", database), new ColumnConfig().setDefaultValue("null").getDefaultValueObject());
                if (shouldBeImplementation(database)) {
                    assertTrue(this.generatorUnderTest.generateSql(statement, database, null)[0].toSql().startsWith("CREATE TABLE IF NOT EXISTS "));
                }
            }
        }
    }

    @Test
    public void testWithCreateRowDependencies() {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof OracleDatabase) {
                CreateTableStatement statement = new CreateTableStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME, true, true);
                statement.addColumn(COLUMN_NAME1, DataTypeFactory.getInstance().fromDescription("java.sql.Types.TIMESTAMP", database), new ColumnConfig().setDefaultValue("null").getDefaultValueObject());
                if (shouldBeImplementation(database)) {
                    assertTrue(this.generatorUnderTest.generateSql(statement, database, null)[0].toSql().contains("ROWDEPENDENCIES"));
                }
            }
        }
    }

    @Test
    public void testInvalidColumnDataType() {
        Database database = new PostgresDatabase();
        CreateTableStatement statement = new CreateTableStatement("cat", "schema", "some_table");
        // Bad data type
        statement.addColumn("col1", DataTypeFactory.getInstance().fromDescription("CHAR(2", database));
        // Good data type
        statement.addColumn("col2", DataTypeFactory.getInstance().fromDescription("CHAR(2)", database));
        Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);
        // This sql is intended to be invalid
        assertEquals("CREATE TABLE schema.some_table (col1 CHAR(2, col2 CHAR(2))", generatedSql[0].toSql());
    }
}
