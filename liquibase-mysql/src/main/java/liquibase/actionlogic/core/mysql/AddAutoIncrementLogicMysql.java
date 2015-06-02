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
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        DelegateResult result = (DelegateResult) super.execute(action, scope);

        if (action.has(AddAutoIncrementAction.Attr.startWith)) {
            MySQLDatabase database = scope.get(Scope.Attr.database, MySQLDatabase.class);

            result = new DelegateResult(result, new AlterTableAction(
                    action.get(AddAutoIncrementAction.Attr.columnName, ObjectName.class).getContainer(),
                    new StringClauses().append(database.getTableOptionAutoIncrementStartWithClause(action.get(AddAutoIncrementAction.Attr.startWith, BigInteger.class)))));

        }

        return result;
    }
}