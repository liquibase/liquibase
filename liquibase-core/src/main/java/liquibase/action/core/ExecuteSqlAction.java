package liquibase.action.core;

import liquibase.action.ExecuteAction;
import liquibase.action.UpdateAction;

public class ExecuteSqlAction extends AbstractSqlAction implements ExecuteAction {

    public ExecuteSqlAction(String sql) {
        super(sql);
    }
}
