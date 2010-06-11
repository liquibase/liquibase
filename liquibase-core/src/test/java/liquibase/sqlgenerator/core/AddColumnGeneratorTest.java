package liquibase.sqlgenerator.core;

import liquibase.database.core.*;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.sqlgenerator.MockSqlGeneratorChain;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.PrimaryKeyConstraint;
import liquibase.statement.core.AddColumnStatement;
import static org.junit.Assert.*;

public class AddColumnGeneratorTest extends AbstractSqlGeneratorTest<AddColumnStatement> {

    public AddColumnGeneratorTest() throws Exception {
        this(new AddColumnGenerator());
    } 

    protected AddColumnGeneratorTest(SqlGenerator<AddColumnStatement> generatorUnderTest) throws Exception {
        super(generatorUnderTest);
    }

	@Override
	protected AddColumnStatement createSampleSqlStatement() {
		return new AddColumnStatement(null, "table_name", "column_name", "column_type", null);
	}


	@Override
    public void isValid() throws Exception {
        super.isValid();
        AddColumnStatement addPKColumn = new AddColumnStatement(null, "table_name", "column_name", "column_type", null, new PrimaryKeyConstraint("pk_name"));

        assertFalse(generatorUnderTest.validate(addPKColumn, new OracleDatabase(), new MockSqlGeneratorChain()).hasErrors());
        assertTrue(generatorUnderTest.validate(addPKColumn, new CacheDatabase(), new MockSqlGeneratorChain()).getErrorMessages().contains("Cannot add a primary key column"));
        assertTrue(generatorUnderTest.validate(addPKColumn, new H2Database(), new MockSqlGeneratorChain()).getErrorMessages().contains("Cannot add a primary key column"));
        assertTrue(generatorUnderTest.validate(addPKColumn, new DB2Database(), new MockSqlGeneratorChain()).getErrorMessages().contains("Cannot add a primary key column"));
        assertTrue(generatorUnderTest.validate(addPKColumn, new DerbyDatabase(), new MockSqlGeneratorChain()).getErrorMessages().contains("Cannot add a primary key column"));
        assertTrue(generatorUnderTest.validate(addPKColumn, new SQLiteDatabase(), new MockSqlGeneratorChain()).getErrorMessages().contains("Cannot add a primary key column"));

        assertTrue(generatorUnderTest.validate(new AddColumnStatement(null, null, null, null, null, new AutoIncrementConstraint()), new MySQLDatabase(), new MockSqlGeneratorChain()).getErrorMessages().contains("Cannot add a non-primary key identity column"));

        assertTrue(generatorUnderTest.validate(new AddColumnStatement(null, null, null, null, null, new AutoIncrementConstraint()), new MySQLDatabase(), new MockSqlGeneratorChain()).getErrorMessages().contains("Cannot add a non-primary key identity column"));
    }
}