package liquibase.statement.core;

public class AlterTableStatementTest extends AbstractSqStatementTest<AlterTableStatement> {

    @Override
    protected AlterTableStatement createStatementUnderTest() {
        return new AlterTableStatement(null, null, null);
    }
}