package liquibase.statement;

public class SetNullableStatementTest extends AbstractSqStatementTest<SetNullableStatement> {

    @Override
    protected SetNullableStatement createStatementUnderTest() {
        return new SetNullableStatement(null, null, null, null, true);
    }

}
