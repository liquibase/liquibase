package liquibase.statement;

public class DropDefaultValueStatementTest extends AbstractSqStatementTest<DropDefaultValueStatement> {

    protected DropDefaultValueStatement createStatementUnderTest() {
        return new DropDefaultValueStatement(null, null, null, null);
    }

}
