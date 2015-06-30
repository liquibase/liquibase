package liquibase.actionlogic.core.informix;

import liquibase.Scope;
import liquibase.action.core.CreateIndexAction;
import liquibase.actionlogic.core.CreateIndexLogic;
import liquibase.database.Database;
import liquibase.database.core.informix.InformixDatabase;
import liquibase.util.StringClauses;

public class CreateIndexLogicInformix extends CreateIndexLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return InformixDatabase.class;
    }

    @Override
    protected StringClauses generateSql(CreateIndexAction action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);

        String tablespace = action.tablespace;
        if (tablespace != null) {
            clauses.replace(tablespace, "IN " + tablespace);
        }

        return clauses;
    }
}
