package liquibase.statement;

public class InsertStatementTest extends AbstractSqStatementTest {

    @Override
    protected SqlStatement createStatementUnderTest() {
        return new InsertStatement(null, null);
    }

}
