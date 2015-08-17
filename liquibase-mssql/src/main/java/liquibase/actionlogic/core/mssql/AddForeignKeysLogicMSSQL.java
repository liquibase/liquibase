package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.core.AddForeignKeysAction;
import liquibase.actionlogic.core.AddForeignKeysLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.structure.core.ForeignKey;
import liquibase.util.StringClauses;

public class AddForeignKeysLogicMSSQL extends AddForeignKeysLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    protected StringClauses generateSql(ForeignKey foreignKey, AddForeignKeysAction action, Scope scope) {
        return super.generateSql(foreignKey, action, scope)
                .remove("ON DELETE RESTRICT");
    }
}
