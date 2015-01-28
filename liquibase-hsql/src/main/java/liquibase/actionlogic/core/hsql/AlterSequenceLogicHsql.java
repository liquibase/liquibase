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
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope);

        errors.checkForDisallowedField(AlterSequenceAction.Attr.incrementBy, action, scope.get(Scope.Attr.database, Database.class).getShortName());
        errors.checkForDisallowedField(AlterSequenceAction.Attr.maxValue, action, scope.get(Scope.Attr.database, Database.class).getShortName());
        errors.checkForDisallowedField(AlterSequenceAction.Attr.ordered, action, scope.get(Scope.Attr.database, Database.class).getShortName());

        return errors;
    }
}
