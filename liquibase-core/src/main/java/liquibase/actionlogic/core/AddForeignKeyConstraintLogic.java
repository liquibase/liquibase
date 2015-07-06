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
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKeyConstraintType;
import liquibase.structure.core.Table;
import liquibase.util.ObjectUtil;
import liquibase.util.StringClauses;
import liquibase.util.StringUtils;

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
                .append(AddForeignKeyConstraintLogic.Clauses.constraintName, action.foreignKey.getSimpleName())
                .append("FOREIGN KEY")
                .append(Clauses.baseColumnNames, "(" + StringUtils.join(action.foreignKey.foreignKeyColumns, ", ", new StringUtils.ObjectSimpleNameFormatter(Column.class, scope.getDatabase())) + ")")
                .append("REFERENCES")
                .append(Clauses.referencedTableName, database.escapeObjectName(action.foreignKey.foreignKeyColumns.get(0).container, Table.class))
                .append(Clauses.referencedColumnNames, "(" + StringUtils.join(action.foreignKey.primaryKeyColumns, ", ", new StringUtils.ObjectSimpleNameFormatter(Column.class, scope.getDatabase())) + ")");

        if (action.foreignKey.updateRule != null) {
            clauses.append("ON UPDATE");
        }

        if (action.foreignKey.deleteRule != null) {
            clauses.append("ON DELETE " + action.foreignKey.deleteRule);
        }


        boolean deferrable = ObjectUtil.defaultIfEmpty(action.foreignKey.deferrable, false);
        boolean initiallyDeferred = ObjectUtil.defaultIfEmpty(action.foreignKey.initiallyDeferred, false);
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
                action.foreignKey.foreignKeyColumns.get(0).container,
                generateSql(action, scope)
        ));
    }
}
