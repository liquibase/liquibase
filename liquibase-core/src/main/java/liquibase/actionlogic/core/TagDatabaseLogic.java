package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.core.TagDatabaseAction;
import liquibase.action.core.UpdateDataAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.Table;
import liquibase.util.StringClauses;

public class TagDatabaseLogic extends AbstractActionLogic<TagDatabaseAction> {

    @Override
    protected Class<TagDatabaseAction> getSupportedAction() {
        return TagDatabaseAction.class;
    }

    @Override
    public ValidationErrors validate(TagDatabaseAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("tag", action);
    }

    @Override
    public ActionResult execute(TagDatabaseAction action, Scope scope) throws ActionPerformException {
        Database database = scope.getDatabase();
        UpdateDataAction updateDataAction = new UpdateDataAction(new ObjectReference(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName()));
        updateDataAction.addNewColumnValue("TAG", action.tag);
        updateDataAction.whereClause = generateWhereClause(action, scope);

        return new DelegateResult(updateDataAction);
    }

    protected StringClauses generateWhereClause(TagDatabaseAction action, Scope scope) {
        Database database = scope.getDatabase();
        return new StringClauses().append("DATEEXECUTED = (SELECT MAX(DATEEXECUTED) FROM " + database.escapeObjectName(new ObjectReference(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName()), Table.class) + ")");
    }
}
