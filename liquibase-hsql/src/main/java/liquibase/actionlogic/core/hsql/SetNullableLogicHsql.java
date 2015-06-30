package liquibase.actionlogic.core.hsql;

import liquibase.Scope;
import liquibase.action.core.SetNullableAction;
import liquibase.actionlogic.core.SetNullableLogic;
import liquibase.database.Database;
import liquibase.database.core.hsql.HsqlDatabase;
import liquibase.util.StringClauses;

public class SetNullableLogicHsql extends SetNullableLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return HsqlDatabase.class;
    }


    @Override
    protected StringClauses generateSql(SetNullableAction action, Scope scope) {
        return super.generateSql(action, scope)
                .prepend("SET");
    }

}
