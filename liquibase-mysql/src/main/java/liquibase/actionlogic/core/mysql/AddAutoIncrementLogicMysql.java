package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddAutoIncrementAction;
import liquibase.action.core.AlterTableAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.AddAutoIncrementLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectName;

import java.math.BigInteger;

public class AddAutoIncrementLogicMysql extends AddAutoIncrementLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }

    @Override
    public ActionResult execute(AddAutoIncrementAction action, Scope scope) throws ActionPerformException {
        DelegateResult result = (DelegateResult) super.execute(action, scope);

        if (action.startWith != null) {
            MySQLDatabase database = scope.get(Scope.Attr.database, MySQLDatabase.class);

            result = new DelegateResult(result, new AlterTableAction(
                    action.columnName.container,
                    new StringClauses().append(database.getTableOptionAutoIncrementStartWithClause(action.startWith))));

        }

        return result;
    }
}