package liquibase.actionlogic.core.derby;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.DropSequenceLogic;
import liquibase.database.Database;
import liquibase.database.core.derby.DerbyDatabase;

public class DropSequenceLogicDerby extends DropSequenceLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return DerbyDatabase.class;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        return super.generateSql(action, scope)
                .append("RESTRICT");
    }
}
