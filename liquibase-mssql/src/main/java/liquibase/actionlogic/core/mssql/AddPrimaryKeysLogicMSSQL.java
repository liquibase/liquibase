package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.core.AddPrimaryKeysAction;
import liquibase.actionlogic.core.AddPrimaryKeysLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.PrimaryKey;
import liquibase.util.ObjectUtil;
import liquibase.util.StringClauses;

public class AddPrimaryKeysLogicMSSQL extends AddPrimaryKeysLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    public ValidationErrors validate(AddPrimaryKeysAction action, Scope scope) {
        ValidationErrors validate = super.validate(action, scope);
        validate.removeUnsupportedField("clustered");
        return validate;
    }

    @Override
    protected StringClauses generateSql(PrimaryKey pk, AddPrimaryKeysAction action, Scope scope) {
        StringClauses clauses = super.generateSql(pk, action, scope);

        if (ObjectUtil.defaultIfEmpty(pk.clustered, true)) {
            clauses.insertAfter("PRIMARY KEY", "CLUSTERED");
        } else {
            clauses.insertAfter("PRIMARY KEY", "NONCLUSTERED");
        }

        String tablespace = pk.tablespace;
        if (tablespace != null) {
            clauses.replace(Clauses.tablespace, "ON " + tablespace);
        }

        return clauses;
    }
}
