package liquibase.actionlogic.core.h2;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.CreateSequenceAction;
import liquibase.actionlogic.core.CreateSequenceLogic;
import liquibase.database.Database;
import liquibase.database.core.h2.H2Database;
import liquibase.exception.ValidationErrors;

public class CreateSequenceLogicH2 extends CreateSequenceLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return H2Database.class;
    }

    @Override
    public ValidationErrors validate(CreateSequenceAction action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope);

        String shortName = scope.getDatabase().getShortName();

        errors.checkForDisallowedField("minValue", action, shortName);
        errors.checkForDisallowedField("maxValue", action, shortName);

        return errors;
    }
}
