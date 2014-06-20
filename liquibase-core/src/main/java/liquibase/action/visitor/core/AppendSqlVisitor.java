package liquibase.action.visitor.core;

import liquibase.action.visitor.AbstractSqlVisitor;
import liquibase.executor.ExecutionOptions;

/**
 * ActionVisitor that appends the given value to SQL in an Action.
 */
public class AppendSqlVisitor extends AbstractSqlVisitor {
    private String value;


    @Override
    public String getName() {
        return "append";
    }

    /**
     * Return value to append. Default value is null
     */
    public String getValue() {
        return value;
    }

    /**
     * Set value to append.
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    protected String modifySql(String sql, ExecutionOptions options) {
        if (value == null) {
            return sql;
        } else {
            return sql + value;
        }
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

}