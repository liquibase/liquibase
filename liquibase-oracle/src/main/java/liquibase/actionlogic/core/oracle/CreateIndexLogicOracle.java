package liquibase.actionlogic.core.oracle;

import liquibase.Scope;
import liquibase.action.core.CreateIndexAction;
import liquibase.actionlogic.core.CreateIndexLogic;
import liquibase.database.Database;
import liquibase.database.core.oracle.OracleDatabase;
import liquibase.util.ObjectUtil;
import liquibase.util.StringClauses;

public class CreateIndexLogicOracle extends CreateIndexLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return OracleDatabase.class;
    }

    @Override
    protected StringClauses generateSql(CreateIndexAction action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);

        if (ObjectUtil.defaultIfEmpty(action.clustered, false)) {
            clauses.insertAfter("ON", "CLUSTER");
        }

        return clauses;
    }
}
