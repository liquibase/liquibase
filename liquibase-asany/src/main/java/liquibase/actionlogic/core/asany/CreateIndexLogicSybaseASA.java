package liquibase.actionlogic.core.asany;

import liquibase.Scope;
import liquibase.action.core.CreateIndexAction;
import liquibase.actionlogic.core.CreateIndexLogic;
import liquibase.database.Database;
import liquibase.database.core.asany.SybaseASADatabase;
import liquibase.util.StringClauses;

public class CreateIndexLogicSybaseASA extends CreateIndexLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return SybaseASADatabase.class;
    }

    @Override
    protected StringClauses generateSql(CreateIndexAction action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);

        String tablespace = action.tablespace;
        if (tablespace != null) {
            clauses.replace(tablespace, "ON "+tablespace);
        }

        return clauses;
    }
}
