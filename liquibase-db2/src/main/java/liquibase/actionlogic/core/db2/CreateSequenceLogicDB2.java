package liquibase.actionlogic.core.db2;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.CreateSequenceAction;
import liquibase.actionlogic.core.CreateSequenceLogic;
import liquibase.database.Database;
import liquibase.database.core.db2.DB2Database;
import liquibase.exception.ValidationErrors;

public class CreateSequenceLogicDB2 extends CreateSequenceLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return DB2Database.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope);

        String shortName = scope.get(Scope.Attr.database, Database.class).getShortName();

        errors.checkForDisallowedField(CreateSequenceAction.Attr.ordered, action, shortName);

        return errors;
    }
}
