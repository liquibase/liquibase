package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddForeignKeyConstraintAction;
import liquibase.action.core.RedefineTableAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;

public class AddForeignKeyConstraintLogic extends AbstractSqlBuilderLogic{

    public static enum Clauses {
        constraintName,
        baseColumnNames, referencedTableName, referencedColumnNames,
    }

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return AddForeignKeyConstraintAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors validationErrors = super.validate(action, scope);

        Database database = scope.get(Scope.Attr.database, Database.class);

        if (!database.supportsInitiallyDeferrableColumns()) {
            validationErrors.checkForDisallowedField(AddForeignKeyConstraintAction.Attr.initiallyDeferred, action, database.getShortName());
            validationErrors.checkForDisallowedField(AddForeignKeyConstraintAction.Attr.deferrable, action, database.getShortName());
        }

        validationErrors.checkForRequiredField(AddForeignKeyConstraintAction.Attr.baseColumnNames, action);
        validationErrors.checkForRequiredField(AddForeignKeyConstraintAction.Attr.baseTableName, action);
        validationErrors.checkForRequiredField(AddForeignKeyConstraintAction.Attr.referencedColumnNames, action);
        validationErrors.checkForRequiredField(AddForeignKeyConstraintAction.Attr.referencedTableName, action);
        validationErrors.checkForRequiredField(AddForeignKeyConstraintAction.Attr.constraintName, action);

        return validationErrors;
    }

    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);

        StringClauses clauses = new StringClauses()
                .append("ADD CONSTRAINT")
                .append(AddForeignKeyConstraintLogic.Clauses.constraintName, action.get(AddForeignKeyConstraintAction.Attr.constraintName, String.class))
                .append("FOREIGN KEY")
                .append(Clauses.baseColumnNames, "(" + database.escapeColumnNameList(action.get(AddForeignKeyConstraintAction.Attr.baseColumnNames, String.class)) + ")")
                .append("REFERENCES")
                .append(Clauses.referencedTableName, database.escapeTableName(action.get(AddForeignKeyConstraintAction.Attr.referencedTableName, String.class), action.get(AddForeignKeyConstraintAction.Attr.referencedTableSchemaName, String.class), action.get(AddForeignKeyConstraintAction.Attr.referencedTableName, String.class)))
                .append(Clauses.referencedColumnNames, "(" + database.escapeColumnNameList(action.get(AddForeignKeyConstraintAction.Attr.referencedColumnNames, String.class)) + ")");

        if (action.get(AddForeignKeyConstraintAction.Attr.onUpdate, false)) {
            clauses.append("ON UPDATE");
        }

        String onDelete = action.get(AddForeignKeyConstraintAction.Attr.onDelete, String.class);
        if (onDelete != null) {
            clauses.append("ON DELETE " + onDelete);
        }


        boolean deferrable = action.get(AddForeignKeyConstraintAction.Attr.deferrable, false);
        boolean initiallyDeferred = action.get(AddForeignKeyConstraintAction.Attr.initiallyDeferred, false);
        if (deferrable || initiallyDeferred) {
            if (deferrable) {
                clauses.append("DEFERRABLE");
            }

            if (initiallyDeferred) {
                clauses.append("INITIALLY DEFERRED");
            }
        }

        return clauses;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {

        return new DelegateResult(new RedefineTableAction(
                action.get(AddForeignKeyConstraintAction.Attr.baseTableCatalogName, String.class),
                action.get(AddForeignKeyConstraintAction.Attr.baseTableSchemaName, String.class),
                action.get(AddForeignKeyConstraintAction.Attr.baseTableName, String.class),
                generateSql(action, scope)
        ));
    }
}
