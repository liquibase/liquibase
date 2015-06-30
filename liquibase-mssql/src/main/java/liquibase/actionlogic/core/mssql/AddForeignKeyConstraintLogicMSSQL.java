package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.core.AddForeignKeyConstraintAction;
import liquibase.actionlogic.core.AddForeignKeyConstraintLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.util.StringClauses;

public class AddForeignKeyConstraintLogicMSSQL extends AddForeignKeyConstraintLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    protected StringClauses generateSql(AddForeignKeyConstraintAction action, Scope scope) {
        return super.generateSql(action, scope)
                .remove("ON DELETE RESTRICT");
    }
}
