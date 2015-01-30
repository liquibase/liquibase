package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.DropUniqueConstraintActon;
import liquibase.action.core.RedefineTableAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RewriteResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;

public class DropUniqueConstraintLogic extends AbstractSqlBuilderLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return DropUniqueConstraintActon.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(DropUniqueConstraintActon.Attr.tableName, action)
                .checkForRequiredField(DropUniqueConstraintActon.Attr.constraintName, action);
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new RewriteResult(new RedefineTableAction(
                action.get(DropUniqueConstraintActon.Attr.catalogName, String.class),
                action.get(DropUniqueConstraintActon.Attr.schemaName, String.class),
                action.get(DropUniqueConstraintActon.Attr.catalogName, String.class),
                generateSql(action, scope)));
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new StringClauses()
                .append("DROP CONSTRAINT")
                .append(database.escapeConstraintName(action.get(DropUniqueConstraintActon.Attr.constraintName, String.class)));

    }
}
