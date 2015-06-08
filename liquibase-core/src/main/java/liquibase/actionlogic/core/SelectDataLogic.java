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
import liquibase.util.CollectionUtil;
import liquibase.util.ObjectUtil;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SelectDataLogic extends AbstractSqlBuilderLogic<SelectDataAction> {

    @Override
    protected Class<SelectDataAction> getSupportedAction() {
        return SelectDataAction.class;
    }

    @Override
    public ValidationErrors validate(SelectDataAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("tableName", action)
                .checkForRequiredField("selectColumnDefinitions", action);
    }

    @Override
    public ActionResult execute(SelectDataAction action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new QuerySqlAction(generateSql(action, scope).toString()));
    }

    @Override
    protected StringClauses generateSql(SelectDataAction action, Scope scope) {
        final Database database = scope.getDatabase();

        StringClauses clauses = new StringClauses()
        .append("SELECT")
                        .append(StringUtils.join(CollectionUtil.createIfNull(action.selectColumnDefinitions), ", ", new StringUtils.StringUtilsFormatter<ColumnDefinition>() {
                    @Override
                    public String toString(ColumnDefinition column) {
                        String columnName = column.columnName.name;
                        if (ObjectUtil.defaultIfEmpty(column.computed, false)) {
                            return columnName;
                        } else {
                            return database.escapeObjectName(columnName, Column.class);
                        }
                    }
                }))
                   .append("FROM")
                        .append(database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName()));

        StringClauses whereClause = action.where;
        if (whereClause != null) {
            clauses.append("WHERE").append(whereClause.toString());
        }

        List<String> orderByColumns = CollectionUtil.createIfNull(action.orderByColumnNames);
        if (orderByColumns.size() > 0) {
            clauses.append("ORDER BY").append(StringUtils.join(orderByColumns, ", "));
        }

        return clauses;
    }
}