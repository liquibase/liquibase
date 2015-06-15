package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.DeleteDataAction;
import liquibase.action.core.RemoveChangeSetRunStatusAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectName;

public class RemoveChangeSetRanStatusLogic extends AbstractActionLogic<RemoveChangeSetRunStatusAction> {

    @Override
    protected Class<RemoveChangeSetRunStatusAction> getSupportedAction() {
        return RemoveChangeSetRunStatusAction.class;
    }

    @Override
    public ValidationErrors validate(RemoveChangeSetRunStatusAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("changeSet", action);
    }

    @Override
    public ActionResult execute(RemoveChangeSetRunStatusAction action, Scope scope) throws ActionPerformException {
        Database database = scope.getDatabase();
        ChangeSet changeSet = action.changeSet;

        DeleteDataAction deleteDataAction = new DeleteDataAction(new ObjectName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName()))
                .addWhereParameters(changeSet.getId(), changeSet.getAuthor(), changeSet.getFilePath());
        deleteDataAction.where = new StringClauses().append("ID=? AND AUTHOR=? AND FILENAME=?");
        return new DelegateResult(deleteDataAction);
    }
}
