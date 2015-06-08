package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddPrimaryKeyAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.AddPrimaryKeyLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.util.ObjectUtil;

public class AddPrimaryKeyLogicMSSQL extends AddPrimaryKeyLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    public ValidationErrors validate(AddPrimaryKeyAction action, Scope scope) {
        ValidationErrors validate = super.validate(action, scope);
        validate.removeUnsupportedField("clustered");
        return validate;
    }

    @Override
    protected StringClauses generateSql(AddPrimaryKeyAction action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);

        if (ObjectUtil.defaultIfEmpty(action.clustered, true)) {
            clauses.insertAfter("PRIMARY KEY", "CLUSTERED");
        } else {
            clauses.insertAfter("PRIMARY KEY", "NONCLUSTERED");
        }

        String tablespace = action.tablespace;
        if (tablespace != null) {
            clauses.replace(Clauses.tablespace, "ON " + tablespace);
        }

        return clauses;
    }
}
