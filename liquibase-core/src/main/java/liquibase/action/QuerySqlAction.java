package liquibase.action;

/**
 * Describes a SQL-based query action.
 */
public class QuerySqlAction extends AbstractSqlAction implements QueryAction {
    public QuerySqlAction(String sql) {
        super(sql);
    }
}
