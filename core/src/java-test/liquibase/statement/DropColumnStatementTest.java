package liquibase.statement;

public class DropColumnStatementTest extends AbstractSqStatementTest {

    protected SqlStatement createStatementUnderTest() {
        return new DropColumnStatement(null, null, null);
    }

}