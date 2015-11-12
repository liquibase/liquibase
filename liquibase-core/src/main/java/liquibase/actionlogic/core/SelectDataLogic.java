package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.QuerySqlAction;
import liquibase.action.core.SelectDataAction;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.Column;
import liquibase.util.CollectionUtil;
import liquibase.util.StringClauses;
import liquibase.util.StringUtils;

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
                .checkForRequiredField("selectColumns", action);
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
                        .append(StringUtils.join(CollectionUtil.createIfNull(action.selectColumns), ", ", new StringUtils.StringUtilsFormatter<Column>() {
                    @Override
                    public String toString(Column column) {
                        String columnName = column.getName();
                        if (column.virtual) {
                            return columnName;
                        } else {
                            return database.escapeObjectName(columnName, Column.class);
                        }
                    }
                }))
                   .append("FROM")
                        .append(database.escapeObjectName(new ObjectReference(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())));

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