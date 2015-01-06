package liquibase.action;

/**
 * Describes a SQL-based action that is neither a query nor an update of existing data.
 * Those types of queries should use {@link QuerySqlAction} or {@link UpdateSqlAction}.
 */
public class ExecuteSqlAction extends AbstractSqlAction implements Action {

    public ExecuteSqlAction(String sql) {
        super(sql);
    }
}
