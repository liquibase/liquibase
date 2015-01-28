package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddUniqueConstraintAction;
import liquibase.action.core.RedefineTableAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RewriteResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.statement.core.AddUniqueConstraintStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;

public class AddUniqueConstraintLogic extends AbstractActionLogic {

    public static enum Clauses {
        constraintName,
        tablespace
    }

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return AddUniqueConstraintAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors validationErrors = super.validate(action, scope);
        validationErrors.checkForRequiredField(AddUniqueConstraintAction.Attr.tableName, action);
        validationErrors.checkForRequiredField(AddUniqueConstraintAction.Attr.columnNames, action);
        return validationErrors;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new RewriteResult(new RedefineTableAction(
                action.get(AddUniqueConstraintAction.Attr.catalogName, String.class),
                action.get(AddUniqueConstraintAction.Attr.schemaName, String.class),
                action.get(AddUniqueConstraintAction.Attr.tableName, String.class),
                getAlterTableClauses(action, scope)
                ));
    }

    protected StringClauses getAlterTableClauses(Action action, Scope scope) {
        String constraintName = action.get(AddUniqueConstraintAction.Attr.constraintName, String.class);
        Database database = scope.get(Scope.Attr.database, Database.class);

		StringClauses clauses = new StringClauses();
        clauses.append("ADD CONSTRAINT");
        if (constraintName != null) {
            clauses.append(Clauses.constraintName, database.escapeConstraintName(constraintName));
        }
        clauses.append("UNIQUE");
        clauses.append("("+database.escapeColumnNameList(action.get(AddUniqueConstraintAction.Attr.columnNames, String.class))+"");

        if (database.supportsInitiallyDeferrableColumns()) {
            if (action.get(AddUniqueConstraintAction.Attr.deferrable, false)) {
                clauses.append("DEFERRABLE");
            }

            if (action.get(AddUniqueConstraintAction.Attr.initiallyDeferred, false)) {
                clauses.append("INITIALLY DEFERRED");
            }
        }

        if (action.get(AddUniqueConstraintAction.Attr.disabled, false)) {
            clauses.append("DISABLE");
        }

        String tablespace = action.get(AddUniqueConstraintAction.Attr.tablespace, String.class);

        if (tablespace != null && database.supportsTablespaces()) {
            clauses.append(Clauses.tablespace, "USING INDEX TABLESPACE " + tablespace);
        }

        return clauses;

    }

    protected UniqueConstraint getAffectedUniqueConstraint(AddUniqueConstraintStatement statement) {
        UniqueConstraint uniqueConstraint = new UniqueConstraint()
                .setName(statement.getConstraintName())
                .setTable((Table) new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName()));
        int i = 0;
        for (Column column : Column.listFromNames(statement.getColumnNames())) {
            uniqueConstraint.addColumn(i++, column);
        }
        return uniqueConstraint;
    }
}
