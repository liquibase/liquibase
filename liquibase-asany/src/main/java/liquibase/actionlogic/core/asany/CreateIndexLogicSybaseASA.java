package liquibase.actionlogic.core.asany;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.CreateIndexAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.CreateIndexLogic;
import liquibase.database.Database;
import liquibase.database.core.asany.SybaseASADatabase;

public class CreateIndexLogicSybaseASA extends CreateIndexLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return SybaseASADatabase.class;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);

        String tablespace = action.get(CreateIndexAction.Attr.tablespace, String.class);
        if (tablespace != null) {
            clauses.replace(tablespace, "ON "+tablespace);
        }

        return clauses;
    }
}
