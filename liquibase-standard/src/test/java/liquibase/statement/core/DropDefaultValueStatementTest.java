package liquibase.statement.core;

public class DropDefaultValueStatementTest extends AbstractSqStatementTest<DropDefaultValueStatement> {

    @Override
    protected DropDefaultValueStatement createStatementUnderTest() {
        return new DropDefaultValueStatement(null, null, null, null, null);
    }

}
