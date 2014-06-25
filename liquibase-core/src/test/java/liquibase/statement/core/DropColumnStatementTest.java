package liquibase.statement.core;

import liquibase.statement.Statement;

public class DropColumnStatementTest extends AbstractSqStatementTest {

    @Override
    protected Statement createStatementUnderTest() {
        return new DropColumnStatement(null, null, null, null);
    }

}