package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.core.DropPrimaryKeyAction;
import liquibase.action.core.AlterTableAction;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Index;
import liquibase.util.StringClauses;

public class DropPrimaryKeyLogic extends AbstractSqlBuilderLogic<DropPrimaryKeyAction> {

	@Override
	protected Class<DropPrimaryKeyAction> getSupportedAction() {
		return DropPrimaryKeyAction.class;
	}

	@Override
	public ValidationErrors validate(DropPrimaryKeyAction action, Scope scope) {
		return super.validate(action, scope)
				.checkForRequiredField("tableName", action);
    }

	@Override
	public ActionResult execute(DropPrimaryKeyAction action, Scope scope) throws ActionPerformException {
		return new DelegateResult(new AlterTableAction(
				action.tableName,
				generateSql(action, scope)
		));
	}

	@Override
	protected StringClauses generateSql(DropPrimaryKeyAction action, Scope scope) {
		Database database = scope.getDatabase();
		String constraintName = action.constraintName;

		if (constraintName == null) {
			return new StringClauses().append("DROP PRIMARY KEY");
		} else {
			return new StringClauses().append("DROP CONSTRAINT "+database.escapeObjectName(constraintName, Index.class));
		}
	}
}
