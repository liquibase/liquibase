package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.core.DeleteDataAction;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.statement.core.DeleteStatement;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;
import liquibase.util.CollectionUtil;
import liquibase.util.StringClauses;

public class DeleteDataLogic extends AbstractSqlBuilderLogic<DeleteDataAction> {

    @Override
    protected Class<DeleteDataAction> getSupportedAction() {
        return DeleteDataAction.class;
    }

    @Override
    public ValidationErrors validate(DeleteDataAction action, Scope scope) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkForRequiredField("tableName", action);
        return validationErrors;
    }

    @Override
    protected StringClauses generateSql(DeleteDataAction action, Scope scope) {
        Database database = scope.getDatabase();
        StringClauses clauses = new StringClauses();
        clauses.append("DELETE FROM");
        clauses.append(database.escapeObjectName(action.tableName, Table.class));

        StringClauses whereClause = action.where;
        if (whereClause != null) {
            String fixedWhereClause = " WHERE " + whereClause;

            for (String columnName : CollectionUtil.createIfNull(action.whereColumnNames)) {
                fixedWhereClause = fixedWhereClause.replaceFirst(":name", database.escapeObjectName(columnName, Column.class));
            }
            for (Object param : CollectionUtil.createIfNull(action.whereParameters)) {
                fixedWhereClause = fixedWhereClause.replaceFirst("\\?|:value", DataTypeFactory.getInstance().fromObject(param, database).objectToSql(param, database).replaceAll("\\$", "\\$"));
            }

            clauses.append(fixedWhereClause);
        }

        return clauses;
    }

    protected Relation getAffectedTable(DeleteStatement statement) {
        return new Table(new ObjectName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()));
    }
}
