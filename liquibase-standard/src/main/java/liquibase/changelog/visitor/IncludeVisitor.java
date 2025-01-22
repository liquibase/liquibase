package liquibase.changelog.visitor;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.include.ChangeLogIncludeHelper;

/**
 * A visitor class designed to flatten {@link liquibase.include.ChangeLogIncludeAll}
 * and {@link liquibase.include.ChangeLogInclude} objects just before the execution of
 * {@link liquibase.changelog.ChangeSet} instances.
 *
 * @author <a href="https://github.com/cagliostro92">Edoardo Patti</a>
 */
public class IncludeVisitor {

 public void visit(DatabaseChangeLog changeLog) {
  ChangeLogIncludeHelper.flatChangeLogChangeSets(changeLog);
 }
}
