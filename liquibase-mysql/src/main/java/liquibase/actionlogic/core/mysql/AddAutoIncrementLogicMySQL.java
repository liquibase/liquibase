package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddAutoIncrementAction;
import liquibase.action.core.AlterTableAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RewriteResult;
import liquibase.actionlogic.core.AddAutoIncrementLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;
import liquibase.exception.ActionPerformException;

import java.math.BigInteger;

public class AddAutoIncrementLogicMySQL extends AddAutoIncrementLogic {

    @Override
    protected boolean supportsScope(Scope scope) {
        return super.supportsScope(scope) && scope.get(Scope.Attr.database, Database.class) instanceof MySQLDatabase;
    }

    @Override
    public int getPriority() {
        return PRIORITY_SPECIALIZED;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        RewriteResult result = (RewriteResult) super.execute(action, scope);

        if (action.has(AddAutoIncrementAction.Attr.startWith)) {
            MySQLDatabase mysqlDatabase = scope.get(Scope.Attr.database, MySQLDatabase.class);

            result = new RewriteResult(result, new AlterTableAction(action.get(AlterTableAction.Attr.catalogName, String.class),
                    action.get(AddAutoIncrementAction.Attr.schemaName, String.class),
                    action.get(AddAutoIncrementAction.Attr.tableName, String.class),
                    mysqlDatabase.getTableOptionAutoIncrementStartWithClause(action.get(AddAutoIncrementAction.Attr.startWith, BigInteger.class))));

        }

        return result;
    }
}