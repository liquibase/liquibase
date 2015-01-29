package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddPrimaryKeyAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.AddPrimaryKeyLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.exception.ValidationErrors;

public class AddPrimaryKeyLogicMSSQL extends AddPrimaryKeyLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors validate = super.validate(action, scope);
        validate.removeUnsupportedField(AddPrimaryKeyAction.Attr.clustered);
        return validate;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);

        if (action.get(AddPrimaryKeyAction.Attr.clustered, true)) {
            clauses.insertAfter("PRIMARY KEY", "CLUSTERED");
        } else {
            clauses.insertAfter("PRIMARY KEY", "NONCLUSTERED");
        }

        String tablespace = action.get(AddPrimaryKeyAction.Attr.tablespace, String.class);
        if (tablespace != null) {
            clauses.replace(Clauses.tablespace, "ON " + tablespace);
        }

        return clauses;
    }
}
