package liquibase.statement.core;

import liquibase.statement.core.DropDefaultValueStatement;

public class DropDefaultValueStatementTest extends AbstractSqStatementTest<DropDefaultValueStatement> {

    @Override
    protected DropDefaultValueStatement createStatementUnderTest() {
        return new DropDefaultValueStatement(null, null, null, null);
    }

}
