package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.QuerySqlAction;
import liquibase.action.core.ColumnDefinition;
import liquibase.action.core.SelectDataAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Column;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SelectDataLogic extends AbstractSqlBuilderLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return SelectDataAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(SelectDataAction.Attr.tableName, action)
                .checkForRequiredField(SelectDataAction.Attr.selectColumnDefinitions, action);
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new QuerySqlAction(generateSql(action, scope).toString()));
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        final Database database = scope.get(Scope.Attr.database, Database.class);

        StringClauses clauses = new StringClauses()
        .append("SELECT")
                        .append(StringUtils.join(action.get(SelectDataAction.Attr.selectColumnDefinitions, new ArrayList<ColumnDefinition>()), ", ", new StringUtils.StringUtilsFormatter<ColumnDefinition>() {
                    @Override
                    public String toString(ColumnDefinition column) {
                        String columnName = column.get(ColumnDefinition.Attr.columnName, String.class);
                        if (column.get(ColumnDefinition.Attr.computed, false)) {
                            return columnName;
                        } else {
                            return database.escapeObjectName(columnName, Column.class);
                        }
                    }
                }))
                   .append("FROM")
                        .append(database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName()));

        String whereClause = action.get(SelectDataAction.Attr.where, String.class);
        if (whereClause != null) {
            clauses.append("WHERE").append(whereClause);
        }

        List<String> orderByColumns = action.get(SelectDataAction.Attr.orderByColumnNames, new ArrayList<String>());
        if (orderByColumns.size() > 0) {
            clauses.append("ORDER BY").append(StringUtils.join(orderByColumns, ", "));
        }

        return clauses;
    }
}