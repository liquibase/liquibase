package liquibase.statement;

public class TagDatabaseStatementTest extends AbstractSqStatementTest<TagDatabaseStatement> {

    @Override
    protected TagDatabaseStatement createStatementUnderTest() {
        return new TagDatabaseStatement(null);
    }


}