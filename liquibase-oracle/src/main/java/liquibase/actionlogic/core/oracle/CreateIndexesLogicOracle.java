package liquibase.actionlogic.core.oracle;

import liquibase.Scope;
import liquibase.action.core.CreateIndexesAction;
import liquibase.actionlogic.core.CreateIndexesLogic;
import liquibase.database.Database;
import liquibase.database.core.oracle.OracleDatabase;
import liquibase.structure.core.Index;
import liquibase.util.ObjectUtil;
import liquibase.util.StringClauses;

public class CreateIndexesLogicOracle extends CreateIndexesLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return OracleDatabase.class;
    }

    @Override
    protected StringClauses generateSql(Index index, CreateIndexesAction action, Scope scope) {
        StringClauses clauses = super.generateSql(index, action, scope);

        if (ObjectUtil.defaultIfEmpty(index.clustered, false)) {
            clauses.insertAfter("ON", "CLUSTER");
        }

        return clauses;
    }
}
