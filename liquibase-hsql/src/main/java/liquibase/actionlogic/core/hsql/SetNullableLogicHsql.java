package liquibase.actionlogic.core.hsql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.SetNullableLogic;
import liquibase.database.Database;
import liquibase.database.core.hsql.HsqlDatabase;

public class SetNullableLogicHsql extends SetNullableLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return HsqlDatabase.class;
    }


    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        return super.generateSql(action, scope)
                .prepend("SET");
    }

}
