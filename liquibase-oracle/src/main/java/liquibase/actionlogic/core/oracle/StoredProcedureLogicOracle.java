package liquibase.actionlogic.core.oracle;

import liquibase.Scope;
import liquibase.action.core.StoredProcedureAction;
import liquibase.actionlogic.core.StoredProcedureLogic;
import liquibase.database.Database;
import liquibase.database.core.oracle.OracleDatabase;
import liquibase.util.StringClauses;

public class StoredProcedureLogicOracle extends StoredProcedureLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return OracleDatabase.class;
    }

    @Override
    protected StringClauses generateSql(StoredProcedureAction action, Scope scope) {
        return super.generateSql(action, scope)
                .replace("EXEC", "BEGIN")
                .append("END;");
    }
}
