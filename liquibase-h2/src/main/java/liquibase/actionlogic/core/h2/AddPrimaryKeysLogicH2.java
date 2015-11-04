package liquibase.actionlogic.core.h2;

import liquibase.Scope;
import liquibase.action.core.AddPrimaryKeysAction;
import liquibase.actionlogic.core.AddPrimaryKeysLogic;
import liquibase.database.Database;
import liquibase.database.core.h2.H2Database;
import liquibase.structure.core.PrimaryKey;
import liquibase.util.StringClauses;

public class AddPrimaryKeysLogicH2 extends AddPrimaryKeysLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return H2Database.class;
    }

    @Override
    protected StringClauses generateSql(PrimaryKey pk, AddPrimaryKeysAction action, Scope scope) {
        return super.generateSql(pk, action, scope)
                .remove("CONSTRAINT");
    }
}
