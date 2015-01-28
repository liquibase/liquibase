package liquibase.actionlogic.core.h2;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AlterSequenceAction;
import liquibase.actionlogic.core.AlterSequenceLogic;
import liquibase.database.Database;
import liquibase.database.core.h2.H2Database;
import liquibase.exception.ValidationErrors;

public class AlterSequenceLogicH2 extends AlterSequenceLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return H2Database.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope);

        errors.checkForDisallowedField(AlterSequenceAction.Attr.incrementBy, action, scope.get(Scope.Attr.database, Database.class).getShortName());
        errors.checkForDisallowedField(AlterSequenceAction.Attr.maxValue, action, scope.get(Scope.Attr.database, Database.class).getShortName());
        errors.checkForDisallowedField(AlterSequenceAction.Attr.minValue, action, scope.get(Scope.Attr.database, Database.class).getShortName());

        return errors;
    }
}
