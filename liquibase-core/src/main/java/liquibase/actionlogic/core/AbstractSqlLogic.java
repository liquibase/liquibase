package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;

public abstract class AbstractSqlLogic extends AbstractActionLogic {

    @Override
    protected boolean supportsScope(Scope scope) {
        return super.supportsScope(scope) && scope.get(Scope.Attr.database, Database.class) instanceof AbstractJdbcDatabase;
    }
}
