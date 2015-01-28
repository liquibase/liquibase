package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddAutoIncrementAction;
import liquibase.action.core.RedefineTableAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RewriteResult;
import liquibase.actionlogic.core.AddAutoIncrementLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;
import liquibase.exception.ActionPerformException;

import java.math.BigInteger;

public class AddAutoIncrementLogicMySQL extends AddAutoIncrementLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        RewriteResult result = (RewriteResult) super.execute(action, scope);

        if (action.has(AddAutoIncrementAction.Attr.startWith)) {
            MySQLDatabase database = scope.get(Scope.Attr.database, MySQLDatabase.class);

            result = new RewriteResult(result, new RedefineTableAction(action.get(RedefineTableAction.Attr.catalogName, String.class),
                    action.get(AddAutoIncrementAction.Attr.schemaName, String.class),
                    action.get(AddAutoIncrementAction.Attr.tableName, String.class),
                    new StringClauses().append(database.getTableOptionAutoIncrementStartWithClause(action.get(AddAutoIncrementAction.Attr.startWith, BigInteger.class)))));

        }

        return result;
    }
}