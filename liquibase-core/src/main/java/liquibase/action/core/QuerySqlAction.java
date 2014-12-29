package liquibase.action.core;

import liquibase.action.QueryAction;
import liquibase.action.UpdateAction;

public class QuerySqlAction extends AbstractSqlAction implements QueryAction {
    public QuerySqlAction(String sql) {
        super(sql);
    }
}
