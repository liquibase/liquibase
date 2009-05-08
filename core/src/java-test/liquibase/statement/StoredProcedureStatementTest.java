package liquibase.statement;

public class StoredProcedureStatementTest extends AbstractSqStatementTest<StoredProcedureStatement> {
    protected StoredProcedureStatement createStatementUnderTest() {
        return new StoredProcedureStatement(null);
    }
}
