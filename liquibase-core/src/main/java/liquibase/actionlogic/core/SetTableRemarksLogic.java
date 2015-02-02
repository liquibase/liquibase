package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.RedefineTableAction;
import liquibase.action.core.SetTableRemarksAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;

public class SetTableRemarksLogic extends AbstractSqlBuilderLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return SetTableRemarksAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(SetTableRemarksAction.Attr.tableName, action)
                .checkForRequiredField(SetTableRemarksAction.Attr.remarks, action);
	}

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new RedefineTableAction(
                action.get(SetTableRemarksAction.Attr.catalogName, String.class),
                action.get(SetTableRemarksAction.Attr.schemaName, String.class),
                action.get(SetTableRemarksAction.Attr.tableName, String.class),
                generateSql(action, scope)));
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new StringClauses()
                .append("COMMENT ON TABLE")
                .append(database.escapeTableName(
                action.get(SetTableRemarksAction.Attr.catalogName, String.class),
                action.get(SetTableRemarksAction.Attr.schemaName, String.class),
                action.get(SetTableRemarksAction.Attr.tableName, String.class)))
                .append("IS")
                .append(database.escapeStringForDatabase(action.get(SetTableRemarksAction.Attr.remarks, String.class)));
    }
}
