package liquibase.statement;

public class AddForeignKeyConstraintStatementTest extends AbstractSqStatementTest<AddForeignKeyConstraintStatement> {

    protected AddForeignKeyConstraintStatement createStatementUnderTest() {
        return new AddForeignKeyConstraintStatement(null, null, null, null, null, null, null);
    }

}
