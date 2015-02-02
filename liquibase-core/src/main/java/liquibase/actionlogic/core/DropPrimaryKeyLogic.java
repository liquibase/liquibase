package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.DropPrimaryKeyAction;
import liquibase.action.core.RedefineTableAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;

public class DropPrimaryKeyLogic extends AbstractSqlBuilderLogic {

	@Override
	protected Class<? extends Action> getSupportedAction() {
		return DropPrimaryKeyAction.class;
	}

	@Override
	public ValidationErrors validate(Action action, Scope scope) {
		return super.validate(action, scope)
				.checkForRequiredField(DropPrimaryKeyAction.Attr.tableName, action);
    }

	@Override
	public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
		return new DelegateResult(new RedefineTableAction(
				action.get(DropPrimaryKeyAction.Attr.catalogName, String.class),
				action.get(DropPrimaryKeyAction.Attr.schemaName, String.class),
				action.get(DropPrimaryKeyAction.Attr.tableName, String.class),
				generateSql(action, scope)
		));
	}

	@Override
	protected StringClauses generateSql(Action action, Scope scope) {
		Database database = scope.get(Scope.Attr.database, Database.class);
		String constraintName = action.get(DropPrimaryKeyAction.Attr.constraintName, String.class);

		if (constraintName == null) {
			return new StringClauses().append("DROP PRIMARY KEY");
		} else {
			return new StringClauses().append("DROP CONSTRAINT "+database.escapeConstraintName(constraintName));
		}
	}
}
