package liquibase.sqlgenerator.core;

import static org.junit.Assert.assertEquals;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.sqlgenerator.MockSqlGeneratorChain;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.AlterTableStatement;
import liquibase.statement.core.DropColumnStatement;

import org.junit.Test;

public class AlterTableGeneratorTest extends AbstractSqlGeneratorTest<AlterTableStatement> {
    private static final String SCHEMA_NAME = "schema_name";
    private static final String TABLE_NAME = "table_name";
    private static final String COLUMN1_NAME = "column1_name";
    private static final String COLUMN2_NAME = "column2_name";
    private static final String COLUMN_TYPE = "column_type";

    public AlterTableGeneratorTest() throws Exception {
        this(new AlterTableGenerator());
    }

    protected AlterTableGeneratorTest(SqlGenerator<AlterTableStatement> generatorUnderTest) throws Exception {
        super(generatorUnderTest);
    }

    @Override
    protected AlterTableStatement createSampleSqlStatement() {
        AlterTableStatement result = new AlterTableStatement(null, SCHEMA_NAME, TABLE_NAME);
        result.addColumn(new AddColumnStatement(null, null, TABLE_NAME, COLUMN1_NAME, COLUMN_TYPE, null));
        return result;
    }

    @Override
    protected boolean shouldBeImplementation(Database database) {
        return database instanceof MySQLDatabase;
    }

    @Test
    public void testMySQLAddTwoColumns() {
        AlterTableStatement statement = new AlterTableStatement(null, SCHEMA_NAME, TABLE_NAME);
        statement.addColumn(new AddColumnStatement(null, null, TABLE_NAME, COLUMN1_NAME, COLUMN_TYPE, null));
        statement.addColumn(new AddColumnStatement(null, null, TABLE_NAME, COLUMN2_NAME, COLUMN_TYPE, null));

        Sql[] sql = generatorUnderTest.generateSql(statement, new MySQLDatabase(), new MockSqlGeneratorChain());
        assertEquals(1, sql.length);

        assertEquals(
                "ALTER TABLE schema_name.table_name ADD column1_name COLUMN_TYPE NULL, ADD column2_name COLUMN_TYPE NULL",
                sql[0].toSql());
    }

    @Test
    public void testMySQLDropTwoColumns() {
        AlterTableStatement statement = new AlterTableStatement(null, SCHEMA_NAME, TABLE_NAME);
        statement.dropColumn(new DropColumnStatement(null, null, TABLE_NAME, COLUMN1_NAME));
        statement.dropColumn(new DropColumnStatement(null, null, TABLE_NAME, COLUMN2_NAME));

        Sql[] sql = generatorUnderTest.generateSql(statement, new MySQLDatabase(), new MockSqlGeneratorChain());
        assertEquals(1, sql.length);

        assertEquals(
                "ALTER TABLE schema_name.table_name DROP COLUMN column1_name, DROP COLUMN column2_name",
                sql[0].toSql());
    }
}
