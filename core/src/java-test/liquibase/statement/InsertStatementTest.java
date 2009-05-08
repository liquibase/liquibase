package liquibase.statement;

public class InsertStatementTest extends AbstractSqStatementTest {

    protected SqlStatement createStatementUnderTest() {
        return new InsertStatement(null, null);
    }

}
