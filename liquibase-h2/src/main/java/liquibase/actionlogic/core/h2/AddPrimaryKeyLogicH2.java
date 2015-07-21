package liquibase.actionlogic.core.h2;

import liquibase.Scope;
import liquibase.action.core.AddPrimaryKeyAction;
import liquibase.actionlogic.core.AddPrimaryKeyLogic;
import liquibase.database.Database;
import liquibase.database.core.h2.H2Database;
import liquibase.util.StringClauses;

public class AddPrimaryKeyLogicH2 extends AddPrimaryKeyLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return H2Database.class;
    }

    @Override
    protected StringClauses generateSql(AddPrimaryKeyAction action, Scope scope) {
        return super.generateSql(action, scope)
                .remove("CONSTRAINT");
    }
}
