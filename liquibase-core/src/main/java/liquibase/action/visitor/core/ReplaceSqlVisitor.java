package liquibase.action.visitor.core;

import liquibase.action.visitor.AbstractSqlVisitor;
import liquibase.executor.ExecutionOptions;

/**
 * ActionVisitor that replaces the SQL in an Action with the result of a simple string replacement.
 */
public class ReplaceSqlVisitor extends AbstractSqlVisitor {

    private String replace;
    private String with;

    @Override
    public String getName() {
        return "replace";
    }

    /**
     * Return the substring to use for replacement. Default value is null
     */
    public String getReplace() {
        return replace;
    }

    public void setReplace(String replace) {
        this.replace = replace;
    }

    /**
     * Return the value to replace matched substring(s) with.
     */
    public String getWith() {
        return with;
    }

    public void setWith(String with) {
        this.with = with;
    }

    @Override
    public String modifySql(String sql, ExecutionOptions options) {
        if (getReplace() == null || getWith() == null) {
            return sql;
        } else {
            return sql.replace(getReplace(), getWith());
        }

    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}