package liquibase.action.core;

import liquibase.AbstractExtensibleObject;
import liquibase.action.Action;

public abstract class AbstractSqlAction extends AbstractExtensibleObject implements Action {

    public static enum Attributes {
        sql,
        endDelimiter
    }

    public AbstractSqlAction(String sql) {
        setAttribute(Attributes.sql, sql);
    }

    public String getSql() {
        return getAttribute(Attributes.sql, String.class);
    }

    public String getEndDelimiter() {
        return getAttribute(Attributes.endDelimiter, ";");
    }

    public AbstractSqlAction setEndDelimiter(String endDelimiter) {
        return (AbstractSqlAction) setAttribute(Attributes.endDelimiter, endDelimiter);
    }

    @Override
    public String describe() {
        return getSql();
    }
}
