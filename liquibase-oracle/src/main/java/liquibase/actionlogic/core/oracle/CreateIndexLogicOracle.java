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
    protected StringClauses generateSql(Action action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);

        if (action.get(CreateIndexAction.Attr.clustered, false)) {
            clauses.insertAfter("ON", "CLUSTER");
        }

        return clauses;
    }
}
