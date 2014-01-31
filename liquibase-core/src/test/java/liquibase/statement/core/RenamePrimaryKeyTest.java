package liquibase.statement.core;

public class RenamePrimaryKeyTest extends AbstractSqStatementTest<RenamePrimaryKeyStatement> {
 
  @Override
  protected RenamePrimaryKeyStatement createStatementUnderTest() {
      return new RenamePrimaryKeyStatement(null, null, null, null, null);
  }
}
