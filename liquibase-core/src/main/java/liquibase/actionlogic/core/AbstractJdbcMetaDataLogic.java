package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;

public abstract class AbstractJdbcMetaDataLogic extends AbstractActionLogic {

    @Override
    public int getPriority(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        if (database != null && database instanceof AbstractJdbcDatabase) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NOT_APPLICABLE;
    }
}
