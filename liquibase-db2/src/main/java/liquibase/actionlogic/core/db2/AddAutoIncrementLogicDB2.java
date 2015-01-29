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
    protected Class<? extends Action> getSupportedAction() {
        return AddAutoIncrementAction.class;
    }

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return DB2Database.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors validationErrors = super.validate(action, scope);
        validationErrors.removeRequiredField(AddAutoIncrementAction.Attr.columnDataType);

        return validationErrors;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new StringClauses().append("SET "+
                        database.getAutoIncrementClause(action.get(AddAutoIncrementAction.Attr.startWith, BigInteger.class), action.get(AddAutoIncrementAction.Attr.incrementBy, BigInteger.class))
        );
    }
}