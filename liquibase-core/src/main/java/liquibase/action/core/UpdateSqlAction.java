package liquibase.action.core;

import liquibase.action.UpdateAction;

public class UpdateSqlAction extends AbstractSqlAction implements UpdateAction {

    public UpdateSqlAction(String sql) {
        super(sql);
    }
}
