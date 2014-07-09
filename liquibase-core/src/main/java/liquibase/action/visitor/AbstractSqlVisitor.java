package liquibase.action.visitor;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import  liquibase.ExecutionEnvironment;

/**
 * Convenience subclass of AbstractActionVisitor for dealing with {@link liquibase.action.core.UnparsedSql} actions.
 */
public abstract class AbstractSqlVisitor extends AbstractActionVisitor {

    /**
     * implement this method with logic for modifying the given SQL.
     */
    protected abstract String modifySql(String sql, ExecutionEnvironment env);

    /**
     * Default implementation calls {@link #modifySql(String,  liquibase.ExecutionEnvironment)} only if the passed Action object is an {@link liquibase.action.core.UnparsedSql}
     */
    @Override
    public void visit(Action action, ExecutionEnvironment env) {
        if (action instanceof UnparsedSql) {
            ((UnparsedSql) action).setSql(modifySql(((UnparsedSql) action).getSql(), env));
        }
    }

}
