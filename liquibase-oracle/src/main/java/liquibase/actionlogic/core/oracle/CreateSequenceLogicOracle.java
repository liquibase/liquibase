package liquibase.actionlogic.core.oracle;

import liquibase.Scope;
import liquibase.action.core.CreateSequenceAction;
import liquibase.actionlogic.core.CreateSequenceLogic;
import liquibase.database.Database;
import liquibase.database.core.oracle.OracleDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.util.StringClauses;

import java.math.BigInteger;

public class CreateSequenceLogicOracle extends CreateSequenceLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return OracleDatabase.class;
    }

    @Override
    public ValidationErrors validate(CreateSequenceAction action, Scope scope) {
        ValidationErrors validate = super.validate(action, scope);

        validate.removeUnsupportedField("cacheSize");

        return validate;
    }

    @Override
    protected StringClauses generateSql(CreateSequenceAction action, Scope scope) {
        BigInteger cacheSize = action.cacheSize;

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
