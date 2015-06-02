package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.DropColumnsAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.DropColumnsLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;

public class DropColumnsLogicMysql extends DropColumnsLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new DelegateResult(new ExecuteSqlAction(
                "ALTER TABLE "
                        + database.escapeObjectName(action.get(DropColumnsAction.Attr.tableName, ObjectName.class), Table.class)
                        + " DROP "
                        + StringUtils.join(action.get(DropColumnsAction.Attr.columnNames, String[].class), ", ", new StringUtils.ObjectNameFormatter(Column.class, database))
        ));
    }
}
