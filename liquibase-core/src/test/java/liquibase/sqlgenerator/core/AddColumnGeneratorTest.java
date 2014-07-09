package liquibase.sqlgenerator.core;

import liquibase.ExecutionEnvironment;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.core.*;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.PrimaryKeyConstraint;
import liquibase.statement.core.AddColumnStatement;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

        assertFalse(generatorUnderTest.validate(addPKColumn, new ExecutionEnvironment(new OracleDatabase()), new StatementLogicChain(null)).hasErrors());
        assertTrue(generatorUnderTest.validate(addPKColumn, new ExecutionEnvironment(new H2Database()), new StatementLogicChain(null)).getErrorMessages().contains("Cannot add a primary key column"));
        assertTrue(generatorUnderTest.validate(addPKColumn, new ExecutionEnvironment(new DB2Database()), new StatementLogicChain(null)).getErrorMessages().contains("Cannot add a primary key column"));
        assertTrue(generatorUnderTest.validate(addPKColumn, new ExecutionEnvironment(new DerbyDatabase()), new StatementLogicChain(null)).getErrorMessages().contains("Cannot add a primary key column"));
        assertTrue(generatorUnderTest.validate(addPKColumn, new ExecutionEnvironment(new SQLiteDatabase()), new StatementLogicChain(null)).getErrorMessages().contains("Cannot add a primary key column"));

        assertTrue(generatorUnderTest.validate(new AddColumnStatement(null, null, null, null, null, null, new AutoIncrementConstraint()), new ExecutionEnvironment(new MySQLDatabase()), new StatementLogicChain(null)).getErrorMessages().contains("Cannot add a non-primary key identity column"));

        assertTrue(generatorUnderTest.validate(new AddColumnStatement(null, null, null, null, null, null, new AutoIncrementConstraint()), new ExecutionEnvironment(new MySQLDatabase()), new StatementLogicChain(null)).getErrorMessages().contains("Cannot add a non-primary key identity column"));
    }
	
	@Test
	public void testAddColumnAfter() {
		AddColumnStatement statement = new AddColumnStatement(null, null, TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, null);
		statement.setAddAfterColumn("column_after");

		assertFalse(generatorUnderTest.validate(statement, new ExecutionEnvironment(new MySQLDatabase()), new StatementLogicChain(null)).hasErrors());
	}
}
