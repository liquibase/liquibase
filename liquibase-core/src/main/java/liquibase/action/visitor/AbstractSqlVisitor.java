package liquibase.action.visitor;

import liquibase.action.Action;
import liquibase.action.UnparsedSql;
import liquibase.executor.ExecutionOptions;

/**
 * Convenience subclass of AbstractActionVisitor for dealing with {@link liquibase.action.UnparsedSql} actions.
 */
public abstract class AbstractSqlVisitor extends AbstractActionVisitor {

    /**
     * implement this method with logic for modifying the given SQL.
     */
    protected abstract String modifySql(String sql, ExecutionOptions options);

    /**
     * Default implementation calls {@link #modifySql(String, liquibase.executor.ExecutionOptions)} only if the passed Action object is an {@link liquibase.action.UnparsedSql}
     */
    @Override
    public void visit(Action action, ExecutionOptions options) {
        if (action instanceof UnparsedSql) {
            ((UnparsedSql) action).setSql(modifySql(((UnparsedSql) action).getSql(), options));
        }
    }

}
