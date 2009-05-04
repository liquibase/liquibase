package liquibase.database.statement.generator;

import liquibase.database.Database;
import liquibase.database.MSSQLDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.SybaseASADatabase;
import liquibase.database.SybaseDatabase;
import liquibase.database.statement.AddUniqueConstraintStatement;
import liquibase.test.TestContext;

import org.junit.Test;

public class AddUniqueConstraintGeneratorTDSTest extends
		AddUniqueConstraintGeneratorTest {

	public AddUniqueConstraintGeneratorTDSTest() throws Exception {
		this(new AddUniqueConstraintGeneratorTDS());
	}

	public AddUniqueConstraintGeneratorTDSTest(AddUniqueConstraintGeneratorTDS generatorUnderTest) throws Exception {
		super(generatorUnderTest);
	}

	@Override
	protected boolean shouldBeImplementation(Database database) {
        return  (database instanceof MSSQLDatabase)
			|| (database instanceof SybaseDatabase)
			|| (database instanceof SybaseASADatabase)
		;
	}
	
	@Test @Override
	public void execute_noSchema() throws Exception {
		AddUniqueConstraintStatement statement = new AddUniqueConstraintStatement(null, TABLE_NAME, COLUMN_NAME, CONSTRAINT_NAME);
		testSqlOn("alter table [adduqtest] add constraint [uq_test] unique ([coltomakeuq])", statement, SybaseDatabase.class);
		testSqlOn("alter table [dbo].[adduqtest] add constraint [uq_test] unique ([coltomakeuq])", statement, MSSQLDatabase.class);
		testSqlOn("alter table [adduqtest] add constraint [uq_test] unique ([coltomakeuq])", statement, SybaseASADatabase.class);
	}

	@Test @Override
	public void execute_noConstraintName() throws Exception {
		AddUniqueConstraintStatement statement = new AddUniqueConstraintStatement(null, TABLE_NAME, COLUMN_NAME, null);
		testSqlOnAllExcept("alter table [adduqtest] add unique ([coltomakeuq])", statement, MSSQLDatabase.class);
		testSqlOn("alter table [dbo].[adduqtest] add unique ([coltomakeuq])", statement, MSSQLDatabase.class);
	}
	
	@Test @Override
	public void execute_withSchema() throws Exception {
		AddUniqueConstraintStatement statement = new AddUniqueConstraintStatement(TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME, CONSTRAINT_NAME);
		testSqlOnAll("alter table [liquibaseb].[adduqtest] add constraint [uq_test] unique ([coltomakeuq])", statement);
	}
}
