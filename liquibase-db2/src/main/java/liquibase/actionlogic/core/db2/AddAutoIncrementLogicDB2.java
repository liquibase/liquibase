package liquibase.actionlogic.core.db2;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddAutoIncrementAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.AddAutoIncrementLogic;
import liquibase.database.Database;
import liquibase.database.core.db2.DB2Database;
import liquibase.exception.ValidationErrors;

import java.math.BigInteger;

public class AddAutoIncrementLogicDB2 extends AddAutoIncrementLogic {

    @Override
    protected Class<AddAutoIncrementAction> getSupportedAction() {
        return AddAutoIncrementAction.class;
    }

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return DB2Database.class;
    }

    @Override
    public ValidationErrors validate(AddAutoIncrementAction action, Scope scope) {
        ValidationErrors validationErrors = super.validate(action, scope);
        validationErrors.removeRequiredField("columnDataType");

        return validationErrors;
    }

    @Override
    protected StringClauses generateSql(AddAutoIncrementAction action, Scope scope) {
        Database database = scope.getDatabase();
        return new StringClauses().append("SET "+
                        database.getAutoIncrementClause(action.startWith, action.incrementBy)
        );
    }
}