package liquibase.statement.core;

public class AddPrimaryKeyStatementTest extends AbstractSqStatementTest<AddPrimaryKeyStatement> {

    @Override
    protected AddPrimaryKeyStatement createStatementUnderTest() {
        return new AddPrimaryKeyStatement(null, null, null, null, null);
    }

   
}
