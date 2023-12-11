package liquibase.statement.core;

public class ReorganizeTableStatementTest extends AbstractSqStatementTest<ReorganizeTableStatement> {

    @Override
    protected ReorganizeTableStatement createStatementUnderTest() {
        return new ReorganizeTableStatement(null, null, null);
    }

}
