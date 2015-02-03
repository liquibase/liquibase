package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.ColumnDefinition;
import liquibase.action.core.CreateTableAction;
import liquibase.action.core.SetColumnRemarksAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.CreateTableLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.util.StringUtils;

import java.util.List;

public class CreateTableLogicMySQL extends CreateTableLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        DelegateResult result = (DelegateResult) super.execute(action, scope);

        for (ColumnDefinition column : action.get(CreateTableAction.Attr.columnDefinitions, new ColumnDefinition[0])) {
            String columnRemarks = column.get(ColumnDefinition.Attr.remarks, String.class);
            if (columnRemarks != null) {
                SetColumnRemarksAction remarksAction = (SetColumnRemarksAction) new SetColumnRemarksAction()
                        .set(SetColumnRemarksAction.Attr.catalogName, action.get(CreateTableAction.Attr.catalogName, String.class))
                        .set(SetColumnRemarksAction.Attr.schemaName, action.get(CreateTableAction.Attr.schemaName, String.class))
                        .set(SetColumnRemarksAction.Attr.tableName, action.get(CreateTableAction.Attr.tableName, String.class))
                        .set(SetColumnRemarksAction.Attr.remarks, columnRemarks);
                return new DelegateResult(result, remarksAction);
            }
        }

        return result;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);

        StringClauses clauses = super.generateSql(action, scope);
        String remarks = action.get(CreateTableAction.Attr.remarks, String.class);
        if (remarks != null) {
            clauses.append("COMMENT='" + database.escapeStringForDatabase(remarks) + "'");
        }

        return clauses;
    }

    @Override
    protected StringClauses generateColumnSql(ColumnDefinition column, Action action, Scope scope, List<Action> additionalActions) {
        StringClauses clauses = super.generateColumnSql(column, action, scope, additionalActions);

        Database database = scope.get(Scope.Attr.database, Database.class);
        String remarks = column.get(ColumnDefinition.Attr.remarks, String.class);

        if (remarks != null) {
            clauses.append("COMMENT '" + database.escapeStringForDatabase(remarks) + "'");
        }


        return clauses;
    }
}
