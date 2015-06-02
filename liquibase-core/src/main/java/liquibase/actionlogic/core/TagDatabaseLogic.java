package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.TagDatabaseAction;
import liquibase.action.core.UpdateDataAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectName;

public class TagDatabaseLogic extends AbstractActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return TagDatabaseAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(TagDatabaseAction.Attr.tag, action);
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        UpdateDataAction updateDataAction = new UpdateDataAction(new ObjectName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName()));
        updateDataAction.addNewColumnValue("TAG", action.get(TagDatabaseAction.Attr.tag, String.class));
        updateDataAction.set(UpdateDataAction.Attr.whereClause, generateWhereClause(action, scope));

        return new DelegateResult(updateDataAction);
    }

    protected String generateWhereClause(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return "DATEEXECUTED = (SELECT MAX(DATEEXECUTED) FROM " + database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName()) + ")";
    }
}
