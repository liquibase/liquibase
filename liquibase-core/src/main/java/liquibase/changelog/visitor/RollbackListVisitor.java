package liquibase.changelog.visitor;

public class RollbackListVisitor extends ListVisitor {
  @Override
  public Direction getDirection() {
    return ChangeSetVisitor.Direction.REVERSE;
  }
}
