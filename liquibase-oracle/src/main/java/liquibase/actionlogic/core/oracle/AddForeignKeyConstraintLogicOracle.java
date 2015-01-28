package liquibase.actionlogic.core.oracle;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.AddForeignKeyConstraintLogic;
import liquibase.database.Database;
import liquibase.database.core.oracle.OracleDatabase;
import liquibase.exception.ActionPerformException;

public class AddForeignKeyConstraintLogicOracle extends AddForeignKeyConstraintLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return OracleDatabase.class;
    }

    @Override
    public StringClauses getAlterTableClauses(Action action, Scope scope) throws ActionPerformException {
        return super.getAlterTableClauses(action, scope)
                .remove("ON UPDATE")
                .remove("ON DELETE RESTRICT")
                .remove("ON DELETE NO ACTION")
                ;
    }
}
