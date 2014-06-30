package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class DropDefaultValueStatementTest extends AbstractStatementTest<DropDefaultValueStatement> {

    @Override
    protected DropDefaultValueStatement createObject() {
        return new DropDefaultValueStatement(null, null, null, null, null);
    }

}
