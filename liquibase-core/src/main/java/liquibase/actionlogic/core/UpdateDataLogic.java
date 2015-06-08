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
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;
import liquibase.util.CollectionUtil;

import java.util.Date;
import java.util.List;

public class UpdateDataLogic extends AbstractSqlBuilderLogic<UpdateDataAction> {

    @Override
    protected Class<UpdateDataAction> getSupportedAction() {
        return UpdateDataAction.class;
    }

    @Override
    public ValidationErrors validate(UpdateDataAction action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope)
                .checkForRequiredField("tableName", action)
                .checkForRequiredField("columnNames", action);

        if (CollectionUtil.createIfNull(action.columnNames).size() != CollectionUtil.createIfNull(action.newColumnValues).size()) {
            errors.addError("UpdateData columnNames and newColumnValues must be of the same length");
        }
        return errors;
    }

    @Override
    public ActionResult execute(UpdateDataAction action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new UpdateSqlAction(generateSql(action, scope).toString()));
    }

    @Override
    protected StringClauses generateSql(UpdateDataAction action, Scope scope) {
        Database database = scope.getDatabase();
        List<String> columnNames = action.columnNames;
        List<Object> newValues = action.newColumnValues;
        StringClauses where = action.whereClause;
        List<String> whereColumnNames = CollectionUtil.createIfNull(action.whereColumnNames);
        List<Object> whereParams = CollectionUtil.createIfNull(action.whereParameters);

        StringClauses clauses = new StringClauses()
                .append("UPDATE")
                .append(database.escapeObjectName(action.tableName, Table.class))
                .append("SET");
        for (int i = 0; i < columnNames.size(); i++) {
            clauses.append(
                    database.escapeObjectName(columnNames.get(i), Column.class)
                            + " = "
                            + convertToString(newValues.get(i), database)
                            + (i < columnNames.size() ? ", " : "")
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
