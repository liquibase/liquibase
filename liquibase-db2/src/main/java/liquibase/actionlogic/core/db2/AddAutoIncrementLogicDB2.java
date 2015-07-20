package liquibase.actionlogic.core.db2;

import liquibase.Scope;
import liquibase.action.core.AddAutoIncrementAction;
import liquibase.actionlogic.core.AddAutoIncrementLogic;
import liquibase.database.Database;
import liquibase.database.core.db2.DB2Database;
import liquibase.exception.ValidationErrors;
import liquibase.util.StringClauses;

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
        return super.generateSql(action, scope)
                .replace(Clauses.dataType, "SET");

    }
}