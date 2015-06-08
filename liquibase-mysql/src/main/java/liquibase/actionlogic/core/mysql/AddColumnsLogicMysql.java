package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddColumnsAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.core.AddColumnsLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;
import liquibase.exception.ActionPerformException;

public class AddColumnsLogicMysql extends AddColumnsLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }

    @Override
    public ActionResult execute(AddColumnsAction action, Scope scope) throws ActionPerformException {
        return null;
//todo: support multiple columns in a single alter table
//        String alterTable = generateSingleColumBaseSQL(columns.get(0), database);
//        for (int i = 0; i < columns.size(); i++) {
//            alterTable += getColumnDefinitionClauses(columns.get(i), database);
//            if (i < columns.size() - 1) {
//                alterTable += ",";
//            }
//        }

    }
}
