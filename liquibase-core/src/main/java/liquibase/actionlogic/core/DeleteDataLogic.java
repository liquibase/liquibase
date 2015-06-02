package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.DeleteDataAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.statement.core.DeleteStatement;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;

import java.util.ArrayList;

public class DeleteDataLogic extends AbstractSqlBuilderLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return DeleteDataAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkForRequiredField(DeleteDataAction.Attr.tableName, action);
        return validationErrors;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        StringClauses clauses = new StringClauses();
        clauses.append("DELETE FROM");
        clauses.append(database.escapeObjectName(action.get(DeleteDataAction.Attr.tableName, ObjectName.class), Table.class));

        String whereClause = action.get(DeleteDataAction.Attr.where, String.class);
        if (whereClause != null) {
            String fixedWhereClause = " WHERE " + whereClause;

            for (String columnName : action.get(DeleteDataAction.Attr.whereColumnNames, new ArrayList<String>())) {
                fixedWhereClause = fixedWhereClause.replaceFirst(":name", database.escapeObjectName(columnName, Column.class));
            }
            for (Object param : action.get(DeleteDataAction.Attr.whereParameters, new ArrayList<String>())) {
                fixedWhereClause = fixedWhereClause.replaceFirst("\\?|:value", DataTypeFactory.getInstance().fromObject(param, database).objectToSql(param, database).replaceAll("\\$", "\\$"));
            }

            clauses.append(fixedWhereClause);
        }

        return clauses;
    }

    protected Relation getAffectedTable(DeleteStatement statement) {
        return new Table(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}
