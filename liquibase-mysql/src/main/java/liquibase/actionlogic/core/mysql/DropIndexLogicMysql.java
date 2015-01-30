package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.DropIndexAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RewriteResult;
import liquibase.actionlogic.core.DropIndexLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Index;

public class DropIndexLogicMysql extends DropIndexLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(DropIndexAction.Attr.tableName, action);
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);

        return new RewriteResult(new ExecuteSqlAction(
                "DROP INDEX "
                        + database.escapeObjectName(action.get(DropIndexAction.Attr.indexName, String.class), Index.class)
                        + " ON "
                        + database.escapeTableName(
                        action.get(DropIndexAction.Attr.tableCatalogName, String.class),
                        action.get(DropIndexAction.Attr.tableSchemaName, String.class),
                        action.get(DropIndexAction.Attr.tableName, String.class))));

    }
}
