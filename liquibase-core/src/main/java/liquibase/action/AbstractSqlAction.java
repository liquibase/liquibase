package liquibase.action;

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
        set(Attr.sql, sql);
    }

    @Override
    public String describe() {
        return get(Attr.sql, String.class) + get(Attr.endDelimiter, "");
    }
}
