package liquibase.changelog.visitor;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.include.ChangeLogIncludeUtils;

public class IncludeVisitor {

 public void visit(DatabaseChangeLog changeLog) {
  ChangeLogIncludeUtils.flatChangeLogChangeSets(changeLog);
 }
}
