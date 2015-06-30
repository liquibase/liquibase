package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.core.AlterTableAction;
import liquibase.action.core.SetTableRemarksAction;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Table;
import liquibase.util.StringClauses;

public class SetTableRemarksLogic extends AbstractSqlBuilderLogic<SetTableRemarksAction> {

    @Override
    protected Class<SetTableRemarksAction> getSupportedAction() {
        return SetTableRemarksAction.class;
    }

    @Override
    public ValidationErrors validate(SetTableRemarksAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("tableName", action)
                .checkForRequiredField("remarks", action);
	}

    @Override
    public ActionResult execute(SetTableRemarksAction action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new AlterTableAction(
                action.tableName,
                generateSql(action, scope)));
    }

    @Override
    protected StringClauses generateSql(SetTableRemarksAction action, Scope scope) {
        Database database = scope.getDatabase();
        return new StringClauses()
                .append("COMMENT ON TABLE")
                .append(database.escapeObjectName(action.tableName, Table.class))
                .append("IS")
                .append(database.escapeStringForDatabase(action.remarks));
    }
}
