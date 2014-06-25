package liquibase.statement.core;

import liquibase.statement.Statement;

public class CreateIndexStatementTest extends AbstractSqStatementTest<Statement> {

    @Override
    protected Statement createStatementUnderTest() {
        return new CreateIndexStatement(null, null, null, null, null, null);
    }

}
