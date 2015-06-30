package liquibase.action;

import liquibase.util.StringClauses;

/**
 * Describes a SQL-based action that is neither a query nor an update of existing data.
 * Those types of queries should use {@link QuerySqlAction} or {@link UpdateSqlAction}.
 */
public class ExecuteSqlAction extends AbstractSqlAction {

    public ExecuteSqlAction(String sql) {
        super(sql);
    }

    public ExecuteSqlAction(StringClauses sql) {
        this(sql.toString());
    }
}
