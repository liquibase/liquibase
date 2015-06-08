package liquibase.actionlogic.core.oracle;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.DropTableAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.DropTableLogic;
import liquibase.database.Database;
import liquibase.database.core.oracle.OracleDatabase;

public class DropTableLogicOracle extends DropTableLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return OracleDatabase.class;
    }

    @Override
    protected StringClauses generateSql(DropTableAction action, Scope scope) {
        return super.generateSql(action, scope)
                .replace("CASCADE", "CASCADE CONSTRAINTS");
    }
}
