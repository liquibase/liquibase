package liquibase.statement;

public class ReorganizeTableStatementTest extends AbstractSqStatementTest<ReorganizeTableStatement> {

    protected ReorganizeTableStatement createStatementUnderTest() {
        return new ReorganizeTableStatement(null, null);
    }

}
