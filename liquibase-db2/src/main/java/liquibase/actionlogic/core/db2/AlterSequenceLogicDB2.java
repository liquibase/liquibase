package liquibase.actionlogic.core.db2;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AlterSequenceAction;
import liquibase.actionlogic.core.AlterSequenceLogic;
import liquibase.database.Database;
import liquibase.database.core.db2.DB2Database;
import liquibase.exception.ValidationErrors;

public class AlterSequenceLogicDB2  extends AlterSequenceLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return DB2Database.class;
    }

    @Override
    public ValidationErrors validate(AlterSequenceAction action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope);

        errors.checkForDisallowedField("ordered", action, scope.getDatabase().getShortName());

        return errors;
    }
}
