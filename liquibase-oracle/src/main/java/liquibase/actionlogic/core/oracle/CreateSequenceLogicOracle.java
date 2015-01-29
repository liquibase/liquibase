package liquibase.actionlogic.core.oracle;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.CreateSequenceAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.CreateSequenceLogic;
import liquibase.database.Database;
import liquibase.database.core.oracle.OracleDatabase;
import liquibase.exception.ValidationErrors;

public class CreateSequenceLogicOracle extends CreateSequenceLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return OracleDatabase.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors validate = super.validate(action, scope);

        validate.removeUnsupportedField(CreateSequenceAction.Attr.cacheSize);

        return validate;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        String cacheSize = action.get(CreateSequenceAction.Attr.cacheSize, String.class);

        StringClauses clauses = super.generateSql(action, scope);

        if (cacheSize != null) {
            if (cacheSize.equals("0")) {
                clauses.insertAfter(Clauses.maxValue, "NOCACHE");
            } else {
                clauses.insertAfter(Clauses.maxValue, "CACHE " + cacheSize);
            }
        }

        return clauses;
    }
}
