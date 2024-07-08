package liquibase.sqlgenerator.core;

import liquibase.change.AddColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.database.core.*;
import liquibase.exception.DatabaseException;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.sqlgenerator.MockSqlGeneratorChain;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.*;
import liquibase.statement.core.AddColumnStatement;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class AddColumnGeneratorTest extends AbstractSqlGeneratorTest<AddColumnStatement> {
    private static final String SCHEMA_NAME = "schema_name";
    private static final String CATALOG_NAME = "catalog_name";
    private static final String TABLE_NAME = "table_name";
    private static final String COLUMN_NAME = "column_name";
    private static final String COLUMN_TYPE = "column_type";

    public AddColumnGeneratorTest() throws Exception {
        this(new AddColumnGenerator());
    }

    protected AddColumnGeneratorTest(SqlGenerator<AddColumnStatement> generatorUnderTest) throws Exception {
        super(generatorUnderTest);
    }

    @Override
    protected AddColumnStatement createSampleSqlStatement() {
        return new AddColumnStatement(null, null, TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, null);
    }


    @Override
    public void isValid() throws Exception {
        super.isValid();
        AddColumnStatement addPKColumn = new AddColumnStatement(null, null, TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, null, new PrimaryKeyConstraint("pk_name"));

        assertFalse(generatorUnderTest.validate(addPKColumn, new OracleDatabase(), new MockSqlGeneratorChain()).hasErrors());
        assertFalse(generatorUnderTest.validate(addPKColumn, new H2Database(), new MockSqlGeneratorChain()).hasErrors());
        assertTrue(generatorUnderTest.validate(addPKColumn, new DB2Database(), new MockSqlGeneratorChain()).getErrorMessages().contains("Cannot add a primary key column"));
        assertTrue(generatorUnderTest.validate(addPKColumn, new DerbyDatabase(), new MockSqlGeneratorChain()).getErrorMessages().contains("Cannot add a primary key column"));
        assertTrue(generatorUnderTest.validate(addPKColumn, new SQLiteDatabase(), new MockSqlGeneratorChain()).getErrorMessages().contains("Cannot add a primary key column"));

        assertTrue(generatorUnderTest.validate(new AddColumnStatement(null, null, null, null, null, null, new AutoIncrementConstraint()), new MySQLDatabase(), new MockSqlGeneratorChain()).getErrorMessages().contains("Cannot add a non-primary key identity column"));

        assertTrue(generatorUnderTest.validate(new AddColumnStatement(null, null, null, null, null, null, new AutoIncrementConstraint()), new MySQLDatabase(), new MockSqlGeneratorChain()).getErrorMessages().contains("Cannot add a non-primary key identity column"));

        assertTrue(generatorUnderTest.validate(new AddColumnStatement(
                new AddColumnStatement(null, null, TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, null),
                new AddColumnStatement(null, null, "other_table", "other_column", COLUMN_TYPE, null)
            ), new MySQLDatabase(), new MockSqlGeneratorChain()).getErrorMessages().contains("All columns must be targeted at the same table"));
    }

    @Test
    public void isValidH2Version1() {
        H2Database h2DatabaseV1 = new H2Database() {
            @Override
            public int getDatabaseMajorVersion() {
                return 1;
            }
        };
        assertFalse(generatorUnderTest.validate(createSampleSqlStatement(), h2DatabaseV1, new MockSqlGeneratorChain()).hasErrors());
    }

    @Test
    public void testAddColumnAfter() {
        AddColumnStatement statement = new AddColumnStatement(null, null, TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, null);
        statement.setAddAfterColumn("column_after");

        assertFalse(generatorUnderTest.validate(statement, new MySQLDatabase(), new MockSqlGeneratorChain()).hasErrors());
    }

    @Test
    public void testAddMultipleColumnsMySql() {
        AddColumnStatement columns = new AddColumnStatement(
                new AddColumnStatement(null, null, TABLE_NAME, "column1", "INT", null, new NotNullConstraint()),
                new AddColumnStatement(null, null, TABLE_NAME, "column2", "INT", null, new NotNullConstraint()));

        assertFalse(generatorUnderTest.validate(columns, new MySQLDatabase(), new MockSqlGeneratorChain()).hasErrors());
        Sql[] sql = generatorUnderTest.generateSql(columns, new MySQLDatabase(), new MockSqlGeneratorChain());

        assertEquals(1, sql.length);
        assertEquals("ALTER TABLE " + TABLE_NAME + " ADD column1 INT NOT NULL, ADD column2 INT NOT NULL", sql[0].toSql());

        List<String> actualNames = sql[0].getAffectedDatabaseObjects().stream().map(o -> o.toString()).collect(Collectors.toList());
        List<String> expectedNames = Arrays.asList(new String[]{"table_name.column1", "table_name.column2", "table_name", "DEFAULT"});
        assertTrue(actualNames.containsAll(expectedNames));
        assertTrue(expectedNames.containsAll(actualNames));
    }

    @Test
    public void testAddPeriodColumnMariaDb() {
        AddColumnStatement columns = new AddColumnStatement(null, null, TABLE_NAME, "PERIOD", "INT", null, new NotNullConstraint());

        assertFalse(generatorUnderTest.validate(columns, new MariaDBDatabase(), new MockSqlGeneratorChain()).hasErrors());
        Sql[] sql = generatorUnderTest.generateSql(columns, new MariaDBDatabase(), new MockSqlGeneratorChain());

        assertEquals(1, sql.length);
        assertEquals("ALTER TABLE " + TABLE_NAME + " ADD `PERIOD` INT NOT NULL", sql[0].toSql());
    }

    @Test
    public void testAddPrimaryKeyColumnH2() {
        AddColumnStatement columns = new AddColumnStatement(null, null, TABLE_NAME, "ID", "BIGINT", null, new PrimaryKeyConstraint());

        H2Database h2Database = new H2Database();
        assertFalse(generatorUnderTest.validate(columns, h2Database, new MockSqlGeneratorChain()).hasErrors());
        assertTrue(generatorUnderTest.validate(columns, new H2Database() {
            @Override
            public int getDatabaseMajorVersion() throws DatabaseException {
                return 1;
            }
        }, new MockSqlGeneratorChain()).hasErrors());

        Sql[] sql = generatorUnderTest.generateSql(columns, h2Database, new MockSqlGeneratorChain());


        assertEquals(1, sql.length);
        assertEquals("ALTER TABLE table_name ADD ID BIGINT NOT NULL PRIMARY KEY", sql[0].toSql());
    }

    @Test
    public void testAddForeignKeyWithEmptySpaces() {
        AddColumnStatement columns = new AddColumnStatement(null, null, TABLE_NAME, "ID", "BIGINT", null,
                new ForeignKeyConstraint("fk_name", "table1 ( id )"));

        H2Database h2Database = new H2Database();
        assertFalse(generatorUnderTest.validate(columns, h2Database, new MockSqlGeneratorChain()).hasErrors());

        Sql[] sql = generatorUnderTest.generateSql(columns, h2Database, new MockSqlGeneratorChain());


        assertEquals(2, sql.length);
        assertEquals("ALTER TABLE table_name ADD CONSTRAINT fk_name FOREIGN KEY (ID) REFERENCES table1 (id)", sql[1].toSql());
    }

    @Test
    public void testAddColumnWithNotNullConstraintAndValue() {
        AddColumnChange change = new AddColumnChange();
        change.setTableName(TABLE_NAME);
        change.setSchemaName(SCHEMA_NAME);

        AddColumnConfig column = new AddColumnConfig();
        column.setName("column1");
        column.setType("int8");
        column.setValueNumeric("0");
        column.setConstraints(new ConstraintsConfig().setNullable(false));
        change.addColumn(column);

        AddColumnConfig column2 = new AddColumnConfig();
        column2.setName("column2");
        column2.setType("boolean");
        column2.setValueBoolean("true");
        column2.setConstraints(new ConstraintsConfig().setNullable(false));
        change.addColumn(column2);

        SqlStatement[] statements = change.generateStatements(new MySQLDatabase());
        SqlGeneratorFactory instance = SqlGeneratorFactory.getInstance();
        Sql[] sql = instance.generateSql(statements, new MySQLDatabase());

        assertEquals(5, sql.length);
        assertEquals("ALTER TABLE schema_name.table_name ADD column1 BIGINT NULL, ADD column2 TINYINT NULL", sql[0].toSql());
        assertEquals("UPDATE schema_name.table_name SET column1 = 0", sql[1].toSql());
        assertEquals("UPDATE schema_name.table_name SET column2 = 1", sql[2].toSql());
        assertEquals("ALTER TABLE schema_name.table_name MODIFY column1 BIGINT NOT NULL", sql[3].toSql());
        assertEquals("ALTER TABLE schema_name.table_name MODIFY column2 TINYINT NOT NULL", sql[4].toSql());

        // repeat with MariaDBDatabase which shall result in TINYINT(1) for boolean column (instead of just TINYINT)
        statements = change.generateStatements(new MariaDBDatabase());
        sql = instance.generateSql(statements, new MariaDBDatabase());

        assertEquals(5, sql.length);
        assertEquals("ALTER TABLE schema_name.table_name ADD column1 BIGINT NULL, ADD column2 TINYINT(1) NULL", sql[0].toSql());
        assertEquals("UPDATE schema_name.table_name SET column1 = 0", sql[1].toSql());
        assertEquals("UPDATE schema_name.table_name SET column2 = 1", sql[2].toSql());
        assertEquals("ALTER TABLE schema_name.table_name MODIFY column1 BIGINT NOT NULL", sql[3].toSql());
        assertEquals("ALTER TABLE schema_name.table_name MODIFY column2 TINYINT(1) NOT NULL", sql[4].toSql());
    }
}
