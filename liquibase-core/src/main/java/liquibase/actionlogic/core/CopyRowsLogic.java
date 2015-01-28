package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.CopyRowsAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RewriteResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Column;
import liquibase.util.StringUtils;

public class CopyRowsLogic extends AbstractActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return CopyRowsAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkForRequiredField(CopyRowsAction.Attr.sourceTableName, action);
        validationErrors.checkForRequiredField(CopyRowsAction.Attr.targetTableName, action);
        validationErrors.checkForRequiredField(CopyRowsAction.Attr.sourceColumns, action);
        return validationErrors;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        final Database database = scope.get(Scope.Attr.database, Database.class);

        String[] sourceColumns = action.get(CopyRowsAction.Attr.sourceColumns, String[].class);
        String[] targetColumns = action.get(CopyRowsAction.Attr.targetColumns, String[].class);

        if (targetColumns == null) {
            targetColumns = sourceColumns;
        }

        String sql = "INSERT INTO "
                + database.escapeTableName(
                action.get(CopyRowsAction.Attr.targetTableCatalogName, String.class),
                action.get(CopyRowsAction.Attr.targetTableSchemaName, String.class),
                action.get(CopyRowsAction.Attr.targetTableName, String.class))
                + " ("
                + StringUtils.join(sourceColumns, ", ", new StringUtils.ObjectNameFormatter(Column.class, database))
                + ")  SELECT "
                + StringUtils.join(targetColumns, ", ", new StringUtils.ObjectNameFormatter(Column.class, database))
                + " FROM "
                + database.escapeTableName(
                action.get(CopyRowsAction.Attr.sourceTableCatalogName, String.class),
                action.get(CopyRowsAction.Attr.sourceTableSchemaName, String.class),
                action.get(CopyRowsAction.Attr.sourceTableName, String.class));

        return new RewriteResult(new ExecuteSqlAction(sql));
    }
}
