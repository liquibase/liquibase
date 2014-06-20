package liquibase.action.visitor.core;

import liquibase.action.visitor.AbstractSqlVisitor;
import liquibase.executor.ExecutionOptions;

/**
 * ActionVisitor that prepends the given value to the SQL in an Action.
 */
public class PrependSqlVisitor extends AbstractSqlVisitor {
    private String value;


    @Override
    public String getName() {
        return "prepend";
    }

    /**
     * Return value to prepend. Default value is null
     */
    public String getValue() {
        return value;
    }

    /**
     * Set value to prepend.
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    protected String modifySql(String sql, ExecutionOptions options) {
        if (value == null) {
            return sql;
        } else {
            return value + sql;
        }
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

}
