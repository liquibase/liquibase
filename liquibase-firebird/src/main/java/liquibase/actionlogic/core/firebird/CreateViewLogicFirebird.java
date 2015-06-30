package liquibase.actionlogic.core.firebird;

import liquibase.Scope;
import liquibase.action.core.CreateViewAction;
import liquibase.actionlogic.core.CreateViewLogic;
import liquibase.database.Database;
import liquibase.database.core.firebird.FirebirdDatabase;
import liquibase.util.ObjectUtil;
import liquibase.util.StringClauses;

public class CreateViewLogicFirebird extends CreateViewLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return FirebirdDatabase.class;
    }

    @Override
    protected StringClauses generateSql(CreateViewAction action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);

        if (ObjectUtil.defaultIfEmpty(action.replaceIfExists, false)) {
            clauses.replace(Clauses.createStatement, "RECREATE VIEW");
        }

        return clauses;
    }
}
