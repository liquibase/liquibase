package liquibase.statement;

public class StoredProcedureStatementTest extends AbstractSqStatementTest<StoredProcedureStatement> {
    @Override
    protected StoredProcedureStatement createStatementUnderTest() {
        return new StoredProcedureStatement(null);
    }
}
