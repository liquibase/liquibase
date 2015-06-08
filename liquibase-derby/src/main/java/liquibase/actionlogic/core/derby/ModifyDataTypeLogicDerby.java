package liquibase.actionlogic.core.derby;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.ModifyDataTypeAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.ModifyDataTypeLogic;
import liquibase.database.Database;
import liquibase.database.core.derby.DerbyDatabase;

public class ModifyDataTypeLogicDerby extends ModifyDataTypeLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return DerbyDatabase.class;
    }

    @Override
    protected StringClauses generateSql(ModifyDataTypeAction action, Scope scope) {
        return super.generateSql(action, scope)
                .prepend("SET DATA TYPE");
    }
}
