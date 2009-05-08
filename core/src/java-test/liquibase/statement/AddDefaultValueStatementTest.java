package liquibase.statement;

public class AddDefaultValueStatementTest extends AbstractSqStatementTest<AddDefaultValueStatement> {

    protected AddDefaultValueStatement createStatementUnderTest() {
        return new AddDefaultValueStatement(null, null, null, null, null);
    }


}