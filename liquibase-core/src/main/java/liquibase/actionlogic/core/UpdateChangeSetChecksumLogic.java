package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.UpdateChangeSetChecksumAction;
import liquibase.action.core.UpdateDataAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;

public class UpdateChangeSetChecksumLogic extends AbstractActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return UpdateChangeSetChecksumAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(UpdateChangeSetChecksumAction.Attr.changeSet, action);
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        ChangeSet changeSet = action.get(UpdateChangeSetChecksumAction.Attr.changeSet, ChangeSet.class);
        return new DelegateResult((Action) new UpdateDataAction(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                .addNewColumnValue("MD5SUM", changeSet.generateCheckSum().toString())
                .set(UpdateDataAction.Attr.whereClause, "ID=? AND AUTHOR=? AND FILENAME=?")
                .set(UpdateDataAction.Attr.whereParameters, new Object[]{changeSet.getId(), changeSet.getAuthor(), changeSet.getFilePath()}));
    }
}