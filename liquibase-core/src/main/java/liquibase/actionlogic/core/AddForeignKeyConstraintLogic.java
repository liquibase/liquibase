package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.core.AddForeignKeyConstraintAction;
import liquibase.action.core.AlterTableAction;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Table;
import liquibase.util.ObjectUtil;
import liquibase.util.StringClauses;

public class AddForeignKeyConstraintLogic extends AbstractSqlBuilderLogic<AddForeignKeyConstraintAction> {

    public static enum Clauses {
        constraintName,
        baseColumnNames, referencedTableName, referencedColumnNames,
    }

    @Override
    protected Class<AddForeignKeyConstraintAction> getSupportedAction() {
        return AddForeignKeyConstraintAction.class;
    }

    @Override
    public ValidationErrors validate(AddForeignKeyConstraintAction action, Scope scope) {
        ValidationErrors validationErrors = super.validate(action, scope);

        Database database = scope.getDatabase();

        if (!database.supportsInitiallyDeferrableColumns()) {
            validationErrors.checkForDisallowedField("initiallyDeferred", action, database.getShortName());
            validationErrors.checkForDisallowedField("deferrable", action, database.getShortName());
        }

        validationErrors.checkForRequiredField("baseColumnNames", action);
        validationErrors.checkForRequiredField("baseTableName", action);
        validationErrors.checkForRequiredField("referencedColumnNames", action);
        validationErrors.checkForRequiredField("referencedTableName", action);
        validationErrors.checkForRequiredField("constraintName", action);

        return validationErrors;
    }

    protected StringClauses generateSql(AddForeignKeyConstraintAction action, Scope scope) {
        Database database = scope.getDatabase();

        StringClauses clauses = new StringClauses()
                .append("ADD CONSTRAINT")
                .append(AddForeignKeyConstraintLogic.Clauses.constraintName, action.constraintName)
                .append("FOREIGN KEY")
                .append(Clauses.baseColumnNames, "(" + database.escapeColumnNameList(action.baseColumnNames) + ")")
                .append("REFERENCES")
                .append(Clauses.referencedTableName, database.escapeObjectName(action.referencedTableName, Table.class))
                .append(Clauses.referencedColumnNames, "(" + database.escapeColumnNameList(action.referencedColumnNames) + ")");

        if (ObjectUtil.defaultIfEmpty(action.onUpdate, false)) {
            clauses.append("ON UPDATE");
        }

        String onDelete = action.onDelete;
        if (onDelete != null) {
            clauses.append("ON DELETE " + onDelete);
        }


        boolean deferrable = ObjectUtil.defaultIfEmpty(action.deferrable, false);
        boolean initiallyDeferred = ObjectUtil.defaultIfEmpty(action.initiallyDeferred, false);
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
    public ActionResult execute(AddForeignKeyConstraintAction action, Scope scope) throws ActionPerformException {

        return new DelegateResult(new AlterTableAction(
                action.baseTableName,
                generateSql(action, scope)
        ));
    }
}
