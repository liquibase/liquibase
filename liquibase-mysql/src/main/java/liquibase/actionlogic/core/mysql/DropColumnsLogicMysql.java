package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.DropColumnsAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RewriteResult;
import liquibase.actionlogic.core.DropColumnsLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.structure.core.Column;
import liquibase.util.StringUtils;

public class DropColumnsLogicMysql extends DropColumnsLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new RewriteResult(new ExecuteSqlAction(
                "ALTER TABLE "
                        + database.escapeTableName(action.get(DropColumnsAction.Attr.catalogName, String.class), action.get(DropColumnsAction.Attr.schemaName, String.class), action.get(DropColumnsAction.Attr.tableName, String.class))
                        + " DROP "
                        + StringUtils.join(action.get(DropColumnsAction.Attr.catalogName, String[].class), ", ", new StringUtils.ObjectNameFormatter(Column.class, database))
        ));
    }
}
