package liquibase.statement;

public class DropColumnStatementTest extends AbstractSqStatementTest {

    @Override
    protected SqlStatement createStatementUnderTest() {
        return new DropColumnStatement(null, null, null);
    }

}