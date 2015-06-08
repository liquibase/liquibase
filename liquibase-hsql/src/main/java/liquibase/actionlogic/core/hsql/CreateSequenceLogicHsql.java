package liquibase.actionlogic.core.hsql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.CreateSequenceAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.CreateSequenceLogic;
import liquibase.database.Database;
import liquibase.database.core.hsql.HsqlDatabase;
import liquibase.exception.ValidationErrors;

public class CreateSequenceLogicHsql extends CreateSequenceLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return HsqlDatabase.class;
    }

    @Override
    public ValidationErrors validate(CreateSequenceAction action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope);

        String shortName = scope.getDatabase().getShortName();
        errors.checkForDisallowedField("minValue", action, shortName);
        errors.checkForDisallowedField("maxValue", action, shortName);
        errors.checkForDisallowedField("ordered", action, shortName);

        return errors;
    }

    @Override
    protected StringClauses generateSql(CreateSequenceAction action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);
        clauses.insertAfter(Clauses.sequenceName, "AS BIGINT");
        return clauses;
    }
}
