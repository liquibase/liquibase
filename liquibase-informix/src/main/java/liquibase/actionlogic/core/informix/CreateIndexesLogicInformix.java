package liquibase.actionlogic.core.informix;

import liquibase.Scope;
import liquibase.action.core.CreateIndexesAction;
import liquibase.actionlogic.core.CreateIndexesLogic;
import liquibase.database.Database;
import liquibase.database.core.informix.InformixDatabase;
import liquibase.structure.core.Index;
import liquibase.util.StringClauses;

public class CreateIndexesLogicInformix extends CreateIndexesLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return InformixDatabase.class;
    }

    @Override
    protected StringClauses generateSql(Index index, CreateIndexesAction action, Scope scope) {
        StringClauses clauses = super.generateSql(index, action, scope);

        String tablespace = index.tablespace;
        if (tablespace != null) {
            clauses.replace(tablespace, "IN " + tablespace);
        }

        return clauses;
    }
}
