package liquibase.statement.core;

public class AddDefaultValueStatementTest extends AbstractSqStatementTest<AddDefaultValueStatement> {

    @Override
    protected AddDefaultValueStatement createStatementUnderTest() {
        return new AddDefaultValueStatement(null, null, null, null, null);
    }


}