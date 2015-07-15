package liquibase.actionlogic.core.h2;

import liquibase.Scope;
import liquibase.action.core.AddAutoIncrementAction;
import liquibase.actionlogic.core.AddAutoIncrementLogic;
import liquibase.database.Database;
import liquibase.database.core.h2.H2Database;
import liquibase.exception.ValidationErrors;

public class AddAutoIncrementLogicH2 extends AddAutoIncrementLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return H2Database.class;
    }

    @Override
    public ValidationErrors validate(AddAutoIncrementAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForDisallowedField("incrementBy", action, scope.getDatabase().getShortName());
    }


}
