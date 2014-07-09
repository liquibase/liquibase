package liquibase.action.visitor.core;

import liquibase.action.visitor.AbstractSqlVisitor;
import  liquibase.ExecutionEnvironment;

/**
 * ActionVisitor that replaces the SQL in an Action with the result of a regular expression replacement.
 */
public class RegExpReplaceSqlVisitor extends AbstractSqlVisitor {

    private String replace;
    private String with;

    @Override
    public String getName() {
        return "regExpReplace";
    }

    /**
     * Return the regular expression to use for replacement. Default value is null
     */
    public String getReplace() {
        return replace;
    }

    /**
     * Set the regular expression to use for replacement.
     */
    public void setReplace(String replace) {
        this.replace = replace;
    }

    /**
     * Return the string to replace the value(s) matched by the regular expression with.
     */
    public String getWith() {
        return with;
    }

    public void setWith(String with) {
        this.with = with;
    }

    @Override
    public String modifySql(String sql, ExecutionEnvironment env) {
        if (getReplace() == null || getWith() == null) {
            return sql;
        } else {
            return sql.replaceAll(getReplace(), getWith());
        }
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

}
