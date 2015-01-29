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
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope);

        String shortName = scope.get(Scope.Attr.database, Database.class).getShortName();
        errors.checkForDisallowedField(CreateSequenceAction.Attr.minValue, action, shortName);
        errors.checkForDisallowedField(CreateSequenceAction.Attr.maxValue, action, shortName);
        errors.checkForDisallowedField(CreateSequenceAction.Attr.ordered, action, shortName);

        return errors;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);
        clauses.insertAfter(Clauses.sequenceName, "AS BIGINT");
        return clauses;
    }
}
