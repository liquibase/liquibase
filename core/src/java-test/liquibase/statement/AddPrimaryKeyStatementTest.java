package liquibase.statement;

public class AddPrimaryKeyStatementTest extends AbstractSqStatementTest<AddPrimaryKeyStatement> {

    protected AddPrimaryKeyStatement createStatementUnderTest() {
        return new AddPrimaryKeyStatement(null, null, null, null);
    }

   
}
