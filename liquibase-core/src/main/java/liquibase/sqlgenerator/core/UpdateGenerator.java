package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statement.core.UpdateDataStatement;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.DatabaseFunction;
import liquibase.structure.core.Column;

import java.util.Date;

public class UpdateGenerator extends AbstractSqlGenerator<UpdateDataStatement> {

    @Override
    public ValidationErrors validate(UpdateDataStatement updateDataStatement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", updateDataStatement.getTableName());
        validationErrors.checkRequiredField("columns", updateDataStatement.getColumnNames());
        return validationErrors;
    }

    @Override
    public Action[] generateActions(UpdateDataStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();

        StringBuffer sql = new StringBuffer("UPDATE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " SET");
        for (String column : statement.getColumnNames()) {
            sql.append(" ").append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), column)).append(" = ");
            sql.append(convertToString(statement.getNewColumnValue(column), database));
            sql.append(",");
        }

        int lastComma = sql.lastIndexOf(",");
        if (lastComma >= 0) {
            sql.deleteCharAt(lastComma);
        }
        if (statement.getWhere() != null) {
            String fixedWhereClause = "WHERE " + statement.getWhere().trim();
            for (String columnName : statement.getWhereColumnNames()) {
                if (columnName == null) {
                    continue;
                }
                fixedWhereClause = fixedWhereClause.replaceFirst(":name",
                        database.escapeObjectName(columnName, Column.class));
            }
            for (Object param : statement.getWhereParameters()) {
                fixedWhereClause = fixedWhereClause.replaceFirst("\\?|:value", DataTypeFactory.getInstance().fromObject(param, database).objectToSql(param, database));
            }
            sql.append(" ").append(fixedWhereClause);
        }

        return new Action[]{
                new UnparsedSql(sql.toString())
        };
    }

    private String convertToString(Object newValue, Database database) {
        String sqlString;
        if (newValue == null || newValue.toString().equalsIgnoreCase("NULL")) {
            sqlString = "NULL";
        } else if (newValue instanceof String && !looksLikeFunctionCall(((String) newValue), database)) {
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
