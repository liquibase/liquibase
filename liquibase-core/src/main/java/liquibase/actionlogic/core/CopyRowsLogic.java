package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.CopyRowsAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.NoOpResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Column;
import liquibase.util.StringUtils;

import java.util.List;

public class CopyRowsLogic extends AbstractActionLogic<CopyRowsAction> {

    @Override
    protected Class<CopyRowsAction> getSupportedAction() {
        return CopyRowsAction.class;
    }

    @Override
    public ValidationErrors validate(CopyRowsAction action, Scope scope) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkForRequiredField("sourceTableName", action);
        validationErrors.checkForRequiredField("targetTableName", action);
        validationErrors.checkForRequiredField("sourceColumns", action);
        return validationErrors;
    }

    @Override
    public ActionResult execute(CopyRowsAction action, Scope scope) throws ActionPerformException {
        final Database database = scope.getDatabase();

        if (action.sourceColumns.size() == 0) {
            return new NoOpResult();
        }

        List<String> sourceColumns = action.sourceColumns;
        List<String> targetColumns = action.targetColumns;

        if (targetColumns == null) {
            targetColumns = sourceColumns;
        }

        String sql = "INSERT INTO "
                + database.escapeObjectName(action.targetTableName)
                + " ("
                + StringUtils.join(sourceColumns, ", ", new StringUtils.ObjectNameFormatter(Column.class, database))
                + ")  SELECT "
                + StringUtils.join(targetColumns, ", ", new StringUtils.ObjectNameFormatter(Column.class, database))
                + " FROM "
                + database.escapeObjectName(action.sourceTableName);

        return new DelegateResult(new ExecuteSqlAction(sql));
    }
}
