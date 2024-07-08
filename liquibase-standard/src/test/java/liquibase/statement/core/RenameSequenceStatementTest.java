package liquibase.statement.core;

public class RenameSequenceStatementTest extends AbstractSqStatementTest<RenameSequenceStatement> {
 
  @Override
  protected RenameSequenceStatement createStatementUnderTest() {
      return new RenameSequenceStatement(null, null, null, null);
  }
}
