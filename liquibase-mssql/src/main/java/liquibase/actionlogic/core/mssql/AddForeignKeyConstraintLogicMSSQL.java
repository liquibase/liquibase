package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.AddForeignKeyConstraintLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.exception.ActionPerformException;

public class AddForeignKeyConstraintLogicMSSQL extends AddForeignKeyConstraintLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    public StringClauses getAlterTableClauses(Action action, Scope scope) throws ActionPerformException {
        return super.getAlterTableClauses(action, scope)
                .remove("ON DELETE RESTRICT");
    }
}
