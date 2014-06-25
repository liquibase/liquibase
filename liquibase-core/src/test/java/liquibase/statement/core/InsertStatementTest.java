package liquibase.statement.core;

import liquibase.statement.Statement;

public class InsertStatementTest extends AbstractSqStatementTest {

    @Override
    protected Statement createStatementUnderTest() {
        return new InsertStatement(null, null, null);
    }

}
