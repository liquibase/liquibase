package liquibase.actionlogic.core.firebird;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.CreateSequenceAction;
import liquibase.actionlogic.core.CreateSequenceLogic;
import liquibase.database.Database;
import liquibase.database.core.firebird.FirebirdDatabase;
import liquibase.exception.ValidationErrors;

public class CreateSequenceLogicFirebird extends CreateSequenceLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return FirebirdDatabase.class;
    }

    @Override
    public ValidationErrors validate(CreateSequenceAction action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope);

        String shortName = scope.getDatabase().getShortName();
        errors.checkForDisallowedField("startValue", action, shortName);
        errors.checkForDisallowedField("incrementBy", action, shortName);

        errors.checkForDisallowedField("minValue", action, shortName);
        errors.checkForDisallowedField("maxValue", action, shortName);

        return errors;
    }
}
