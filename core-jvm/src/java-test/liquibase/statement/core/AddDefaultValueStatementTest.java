package liquibase.statement.core;

import liquibase.statement.core.AddDefaultValueStatement;

public class AddDefaultValueStatementTest extends AbstractSqStatementTest<AddDefaultValueStatement> {

    @Override
    protected AddDefaultValueStatement createStatementUnderTest() {
        return new AddDefaultValueStatement(null, null, null, null, null);
    }


}