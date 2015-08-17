package liquibase.action;

import liquibase.util.StringClauses;

/**
 * Describes a SQL-based query action.
 */
public class QuerySqlAction extends AbstractSqlAction implements QueryAction {
    public QuerySqlAction(String sql) {
        super(sql);
    }

    public QuerySqlAction(StringClauses sql) {
        super(sql);
    }
}
