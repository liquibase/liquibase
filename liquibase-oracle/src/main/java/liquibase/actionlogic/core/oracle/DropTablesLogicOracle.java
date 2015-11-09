package liquibase.actionlogic.core.oracle;

import liquibase.Scope;
import liquibase.action.core.DropTablesAction;
import liquibase.actionlogic.core.DropTablesLogic;
import liquibase.database.Database;
import liquibase.database.core.oracle.OracleDatabase;
import liquibase.structure.ObjectReference;
import liquibase.util.StringClauses;

public class DropTablesLogicOracle extends DropTablesLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return OracleDatabase.class;
    }

    @Override
    protected StringClauses generateSql(ObjectReference table, DropTablesAction action, Scope scope) {
        return super.generateSql(table, action, scope)
                .replace("CASCADE", "CASCADE CONSTRAINTS");
    }
}
