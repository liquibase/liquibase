package liquibase.sqlgenerator;

import liquibase.database.*;
import liquibase.statement.*;
import liquibase.test.TestContext;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AddColumnGeneratorTest extends AbstractSqlGeneratorTest<AddColumnStatement> {

    public AddColumnGeneratorTest() throws Exception {
        this(new AddColumnGenerator());
    }

    public AddColumnGeneratorTest(SqlGenerator<AddColumnStatement> generatorUnderTest) throws Exception {
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

        assertFalse(generatorUnderTest.validate(addPKColumn, new OracleDatabase()).hasErrors());
        assertTrue(generatorUnderTest.validate(addPKColumn, new CacheDatabase()).getErrorMessages().contains("Cannot add a primary key column"));
        assertTrue(generatorUnderTest.validate(addPKColumn, new H2Database()).getErrorMessages().contains("Cannot add a primary key column"));
        assertTrue(generatorUnderTest.validate(addPKColumn, new DB2Database()).getErrorMessages().contains("Cannot add a primary key column"));
        assertTrue(generatorUnderTest.validate(addPKColumn, new DerbyDatabase()).getErrorMessages().contains("Cannot add a primary key column"));
        assertTrue(generatorUnderTest.validate(addPKColumn, new SQLiteDatabase()).getErrorMessages().contains("Cannot add a primary key column"));

        assertTrue(generatorUnderTest.validate(new AddColumnStatement(null, null, null, null, null, new AutoIncrementConstraint()), new MySQLDatabase()).getErrorMessages().contains("Cannot add a non-primary key identity column"));

        assertTrue(generatorUnderTest.validate(new AddColumnStatement(null, null, null, null, null, new AutoIncrementConstraint()), new MySQLDatabase()).getErrorMessages().contains("Cannot add a non-primary key identity column"));
    }
}