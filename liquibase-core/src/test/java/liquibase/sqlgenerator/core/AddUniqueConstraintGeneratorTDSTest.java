package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;

public class AddUniqueConstraintGeneratorTDSTest extends
		AddUniqueConstraintGeneratorTest {

	public AddUniqueConstraintGeneratorTDSTest() throws Exception {
		super(new AddUniqueConstraintGeneratorTDS());
	}

	@Override
	protected boolean shouldBeImplementation(Database database) {
        return  (database instanceof MSSQLDatabase)
			|| (database instanceof SybaseDatabase)
			|| (database instanceof SybaseASADatabase)
		;
	}
}
