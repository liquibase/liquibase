package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.CreateTableAction;
import liquibase.action.core.SetColumnRemarksAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.CreateTableLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;
import liquibase.util.CollectionUtil;
import liquibase.util.StringClauses;

import java.util.List;

public class CreateTableLogicMySQL extends CreateTableLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }

    @Override
    public ActionResult execute(CreateTableAction action, Scope scope) throws ActionPerformException {
        DelegateResult result = (DelegateResult) super.execute(action, scope);

        for (Column column : CollectionUtil.createIfNull(action.columns)) {
            String columnRemarks = column.remarks;
            if (columnRemarks != null) {
                SetColumnRemarksAction remarksAction = new SetColumnRemarksAction();
                remarksAction.columnName = new ObjectName(action.tableName, column.getSimpleName());
                remarksAction.remarks = columnRemarks;
                return new DelegateResult(result, remarksAction);
            }
        }

        return result;
    }

    @Override
    protected StringClauses generateSql(CreateTableAction action, Scope scope) {
        Database database = scope.getDatabase();

        StringClauses clauses = super.generateSql(action, scope);
        String remarks = action.remarks;
        if (remarks != null) {
            clauses.append("COMMENT='" + database.escapeStringForDatabase(remarks) + "'");
        }

        return clauses;
    }

    @Override
    protected StringClauses generateColumnSql(Column column, CreateTableAction action, Scope scope, List<Action> additionalActions) {
        StringClauses clauses = super.generateColumnSql(column, action, scope, additionalActions);

        Database database = scope.getDatabase();
        String remarks = column.remarks;

        if (remarks != null) {
            clauses.append("COMMENT '" + database.escapeStringForDatabase(remarks) + "'");
        }


        return clauses;
    }
}
