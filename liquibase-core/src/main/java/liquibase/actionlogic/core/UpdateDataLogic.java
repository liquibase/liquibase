package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.UpdateSqlAction;
import liquibase.action.core.StringClauses;
import liquibase.action.core.UpdateDataAction;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.core.UpdateStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;

import java.util.Date;

public class UpdateDataLogic extends AbstractSqlBuilderLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return UpdateDataAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope)
                .checkForRequiredField(UpdateDataAction.Attr.tableName, action)
                .checkForRequiredField(UpdateDataAction.Attr.columnNames, action);

        if (action.get(UpdateDataAction.Attr.columnNames, String[].class).length != action.get(UpdateDataAction.Attr.newColumnValues, Object[].class).length) {
            errors.addError("UpdateData columnNames and newColumnValues must be of the same length");
        }
        return errors;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new UpdateSqlAction(generateSql(action, scope).toString()));
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        String[] columnNames = action.get(UpdateDataAction.Attr.columnNames, String[].class);
        Object[] newValues = action.get(UpdateDataAction.Attr.newColumnValues, Object[].class);
        String where = action.get(UpdateDataAction.Attr.whereClause, String.class);
        String[] whereColumnNames = action.get(UpdateDataAction.Attr.whereColumnNames, new String[0]);
        Object[] whereParams = action.get(UpdateDataAction.Attr.whereParameters, new Object[0]);

        StringClauses clauses = new StringClauses()
                .append("UPDATE")
                .append(database.escapeTableName(action.get(UpdateDataAction.Attr.catalogName, String.class), action.get(UpdateDataAction.Attr.schemaName, String.class), action.get(UpdateDataAction.Attr.tableName, String.class)))
                .append("SET");
        for (int i = 0; i < columnNames.length; i++) {
            clauses.append(
                    database.escapeObjectName(columnNames[i], Column.class)
                            +" = "
                            + convertToString(newValues[i], database)
                            + (i < columnNames.length?", ": "")
            );
        }

        if (where != null) {
            String fixedWhereClause = "WHERE " + where;
            for (String columnName : whereColumnNames) {
                fixedWhereClause = fixedWhereClause.replaceFirst(":name", database.escapeObjectName(columnName, Column.class));
            }
            for (Object param : whereParams) {
                fixedWhereClause = fixedWhereClause.replaceFirst("\\?|:value", DataTypeFactory.getInstance().fromObject(param, database).objectToSql(param, database));
            }
            clauses.append(fixedWhereClause);
        }

        return clauses;
    }

    protected String convertToString(Object newValue, Database database) {
        String sqlString;
        if (newValue == null || newValue.toString().equalsIgnoreCase("NULL")) {
            sqlString = "NULL";
        } else if (newValue instanceof String && !database.looksLikeFunctionCall((String) newValue)) {
            sqlString = DataTypeFactory.getInstance().fromObject(newValue, database).objectToSql(newValue, database);
        } else if (newValue instanceof Date) {
            // converting java.util.Date to java.sql.Date
            Date date = (Date) newValue;
            if (date.getClass().equals(java.util.Date.class)) {
                date = new java.sql.Date(date.getTime());
            }

            sqlString = database.getDateLiteral(date);
        } else if (newValue instanceof Boolean) {
            if (((Boolean) newValue)) {
                sqlString = DataTypeFactory.getInstance().getTrueBooleanValue(database);
            } else {
                sqlString = DataTypeFactory.getInstance().getFalseBooleanValue(database);
            }
        } else if (newValue instanceof DatabaseFunction) {
            sqlString = database.generateDatabaseFunctionValue((DatabaseFunction) newValue);
        } else {
            sqlString = newValue.toString();
        }
        return sqlString;
    }
}
