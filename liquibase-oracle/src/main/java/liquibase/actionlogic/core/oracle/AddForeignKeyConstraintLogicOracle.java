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
    protected StringClauses generateSql(Action action, Scope scope) {
        return super.generateSql(action, scope)
                .remove("ON UPDATE")
                .remove("ON DELETE RESTRICT")
                .remove("ON DELETE NO ACTION")
                ;
    }
}
