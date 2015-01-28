package liquibase.actionlogic.core.oracle;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.CreateIndexAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.CreateIndexLogic;
import liquibase.database.Database;
import liquibase.database.core.oracle.OracleDatabase;

public class CreateIndexLogicOracle extends CreateIndexLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return OracleDatabase.class;
    }

    @Override
    public StringClauses getCreateIndexClauses(Action action, Scope scope) {
        StringClauses clauses = super.getCreateIndexClauses(action, scope);

        if (action.get(CreateIndexAction.Attr.clustered, false)) {
            clauses.insertAfter("ON", "CLUSTER");
        }

        return clauses;
    }
}
