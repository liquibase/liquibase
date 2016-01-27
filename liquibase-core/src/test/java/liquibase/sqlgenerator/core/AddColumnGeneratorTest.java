package liquibase.sqlgenerator.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import liquibase.database.core.DB2Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.sqlgenerator.MockSqlGeneratorChain;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.PrimaryKeyConstraint;
import liquibase.statement.core.AddColumnStatement;

import org.junit.Test;

public class AddColumnGeneratorTest extends AbstractSqlGeneratorTest<AddColumnStatement> {
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
        assertTrue(generatorUnderTest.validate(addPKColumn, new H2Database(), new MockSqlGeneratorChain()).getErrorMessages().contains("Cannot add a primary key column"));
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
	    assertEquals("[DEFAULT, table_name, table_name.column1, table_name.column2]", String.valueOf(sql[0].getAffectedDatabaseObjects()));
	}
}
