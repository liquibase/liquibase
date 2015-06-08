package liquibase.actionlogic.core.hsql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AlterSequenceAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.AlterSequenceLogic;
import liquibase.database.Database;
import liquibase.database.core.hsql.HsqlDatabase;
import liquibase.exception.ValidationErrors;

public class AlterSequenceLogicHsql extends AlterSequenceLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return HsqlDatabase.class;
    }

    @Override
    public ValidationErrors validate(AlterSequenceAction action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope);

        errors.checkForDisallowedField("incrementBy", action, scope.getDatabase().getShortName());
        errors.checkForDisallowedField("maxValue", action, scope.getDatabase().getShortName());
        errors.checkForDisallowedField("ordered", action, scope.getDatabase().getShortName());

        return errors;
    }
}
