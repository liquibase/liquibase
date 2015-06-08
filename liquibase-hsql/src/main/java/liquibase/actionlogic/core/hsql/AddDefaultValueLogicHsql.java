package liquibase.actionlogic.core.hsql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddDefaultValueAction;
import liquibase.actionlogic.core.AddDefaultValueLogic;
import liquibase.database.Database;
import liquibase.database.core.hsql.HsqlDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceNextValueFunction;

public class AddDefaultValueLogicHsql extends AddDefaultValueLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return HsqlDatabase.class;
    }

    @Override
    public ValidationErrors validate(AddDefaultValueAction action, Scope scope) {
        Object defaultValue = action.defaultValue;
        Database database = scope.getDatabase();

        ValidationErrors errors = super.validate(action, scope);

        if (defaultValue instanceof SequenceNextValueFunction) {
            errors.addError("Database " + database.getShortName() + " does not support adding sequence-based default values");
        } else if (defaultValue instanceof DatabaseFunction) {
            errors.addError("Database " + database.getShortName() + " does not support adding function-based default values");
        }

        return errors;
    }
}
