package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.DeleteDataAction;
import liquibase.action.core.RemoveChangeSetRunStatusAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectName;

public class RemoveChangeSetRanStatusLogic extends AbstractActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return RemoveChangeSetRunStatusAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(RemoveChangeSetRunStatusAction.Attr.changeSet, action);
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        ChangeSet changeSet = action.get(RemoveChangeSetRunStatusAction.Attr.changeSet, ChangeSet.class);

        return new DelegateResult((DeleteDataAction) new DeleteDataAction(new ObjectName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName()))
                .addWhereParameters(changeSet.getId(), changeSet.getAuthor(), changeSet.getFilePath())
                .set(DeleteDataAction.Attr.where, "ID=? AND AUTHOR=? AND FILENAME=?"));
    }
}
