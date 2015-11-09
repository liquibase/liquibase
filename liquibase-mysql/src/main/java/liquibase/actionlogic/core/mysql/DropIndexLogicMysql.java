package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.DropIndexAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.DropIndexLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;

public class DropIndexLogicMysql extends DropIndexLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }

    @Override
    public ValidationErrors validate(DropIndexAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("tableName", action);
    }

    @Override
    public ActionResult execute(DropIndexAction action, Scope scope) throws ActionPerformException {
        Database database = scope.getDatabase();

        return new DelegateResult(new ExecuteSqlAction(
                "DROP INDEX "
                        + database.escapeObjectName(action.indexName, Index.class)
                        + " ON "
                        + database.escapeObjectName(action.tableName, Table.class)));

    }
}
