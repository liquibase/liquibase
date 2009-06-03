package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.MSSQLDatabase;
import liquibase.database.SybaseASADatabase;
import liquibase.database.SybaseDatabase;
import liquibase.sqlgenerator.core.AddUniqueConstraintGeneratorTDS;

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
}
