package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.core.AddColumnsLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;
import liquibase.exception.ActionPerformException;

public class AddColumnsLogicMysql extends AddColumnsLogic {

    @Override
    protected int getPriority() {
        return PRIORITY_SPECIALIZED;
    }

    @Override
    protected boolean supportsScope(Scope scope) {
        return super.supportsScope(scope) && scope.get(Scope.Attr.database, Database.class) instanceof MySQLDatabase;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        String alterTable = generateSingleColumBaseSQL(columns.get(0), database);
        for (int i = 0; i < columns.size(); i++) {
            alterTable += getColumnDefinitionClauses(columns.get(i), database);
            if (i < columns.size() - 1) {
                alterTable += ",";
            }
        }

    }
}
