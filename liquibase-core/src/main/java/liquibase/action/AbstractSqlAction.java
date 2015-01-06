package liquibase.action;

import liquibase.AbstractExtensibleObject;

/**
 * Standard base class for sql-based actions.
 * Normally subclass from the more specific {@link UpdateSqlAction}, {@link QuerySqlAction} and {@link ExecuteSqlAction}.
 */
public abstract class AbstractSqlAction extends AbstractAction {

    public static enum Attr {
        sql,
        endDelimiter
    }

    public AbstractSqlAction(String sql) {
        setAttribute(Attr.sql, sql);
    }

    @Override
    public String describe() {
        return getAttribute(Attr.sql, String.class) + getAttribute(Attr.endDelimiter, "");
    }
}
