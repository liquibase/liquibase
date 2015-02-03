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
    protected StringClauses generateSql(Action action, Scope scope) {
        return super.generateSql(action, scope)
                .remove("ON DELETE RESTRICT");
    }
}
