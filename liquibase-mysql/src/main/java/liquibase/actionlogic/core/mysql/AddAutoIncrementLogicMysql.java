package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.core.AddAutoIncrementAction;
import liquibase.action.core.AlterTableAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.AddAutoIncrementLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.util.StringClauses;

import java.math.BigInteger;

public class AddAutoIncrementLogicMysql extends AddAutoIncrementLogic {

    @Override
    public ValidationErrors validate(AddAutoIncrementAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForDisallowedField("incrementBy", action, scope.getDatabase().getShortName());
    }

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }

    @Override
    public ActionResult execute(AddAutoIncrementAction action, Scope scope) throws ActionPerformException {
        DelegateResult delegate = (DelegateResult) super.execute(action, scope);
        if (action.startWith != null) {
            delegate.addActions(new AlterTableAction(action.columnName.container, new StringClauses().append("AUTO_INCREMENT="+action.startWith)));
        }
        return delegate;
    }

    @Override
    public StringClauses generateAutoIncrementClause(BigInteger startWith, BigInteger incrementBy) {
        return new StringClauses().append("AUTO_INCREMENT");
    }
}