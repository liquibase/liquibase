package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.DropIndexAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.DropIndexLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;

public class DropIndexLogicMSSQL extends DropIndexLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(DropIndexAction.Attr.tableName, action);
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);

        return new DelegateResult(new ExecuteSqlAction(
                "DROP INDEX "
                        + database.escapeObjectName(action.get(DropIndexAction.Attr.tableName, ObjectName.class), Table.class)
                        + "."
                        + database.escapeObjectName(action.get(DropIndexAction.Attr.indexName, ObjectName.class).getName(), Index.class)));


    }
}
