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
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope);

        String shortName = scope.get(Scope.Attr.database, Database.class).getShortName();
        errors.checkForDisallowedField(CreateSequenceAction.Attr.startValue, action, shortName);
        errors.checkForDisallowedField(CreateSequenceAction.Attr.incrementBy, action, shortName);

        errors.checkForDisallowedField(CreateSequenceAction.Attr.minValue, action, shortName);
        errors.checkForDisallowedField(CreateSequenceAction.Attr.maxValue, action, shortName);

        return errors;
    }
}
