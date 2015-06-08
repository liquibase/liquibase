package liquibase.actionlogic.core.oracle;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.DropPrimaryKeyAction;
import liquibase.action.core.StringClauses;
import liquibase.database.Database;
import liquibase.database.core.oracle.OracleDatabase;
import liquibase.actionlogic.core.DropPrimaryKeyLogic;

public class DropPrimaryKeyLogicOracle extends DropPrimaryKeyLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return OracleDatabase.class;
    }

    @Override
    protected StringClauses generateSql(DropPrimaryKeyAction action, Scope scope) {
        return new StringClauses().append("DROP PRIMARY KEY DROP INDEX");
    }
}
