package liquibase.actionlogic.core.derby;

import liquibase.Scope;
import liquibase.action.core.DropSequenceAction;
import liquibase.actionlogic.core.DropSequenceLogic;
import liquibase.database.Database;
import liquibase.database.core.derby.DerbyDatabase;
import liquibase.util.StringClauses;

public class DropSequenceLogicDerby extends DropSequenceLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return DerbyDatabase.class;
    }

    @Override
    protected StringClauses generateSql(DropSequenceAction action, Scope scope) {
        return super.generateSql(action, scope)
                .append("RESTRICT");
    }
}
