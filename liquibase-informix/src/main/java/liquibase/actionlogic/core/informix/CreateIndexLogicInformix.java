package liquibase.actionlogic.core.informix;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.CreateIndexAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.CreateIndexLogic;
import liquibase.database.Database;
import liquibase.database.core.informix.InformixDatabase;

public class CreateIndexLogicInformix extends CreateIndexLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return InformixDatabase.class;
    }

    @Override
    public StringClauses getCreateIndexClauses(Action action, Scope scope) {
        StringClauses clauses = super.getCreateIndexClauses(action, scope);

        String tablespace = action.get(CreateIndexAction.Attr.tablespace, String.class);
        if (tablespace != null) {
            clauses.replace(tablespace, "IN " + tablespace);
        }

        return clauses;
    }
}
