package liquibase.sqlgenerator.core;

import java.util.Date;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.UpdateStatement;

public class UpdateGenerator extends AbstractSqlGenerator<UpdateStatement> {

    public ValidationErrors validate(UpdateStatement updateStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", updateStatement.getTableName());
        validationErrors.checkRequiredField("columns", updateStatement.getNewColumnValues());
        return validationErrors;
    }

    public Sql[] generateSql(UpdateStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
    	   	
        StringBuffer sql = new StringBuffer("UPDATE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " SET");
        for (String column : statement.getNewColumnValues().keySet()) {
            sql.append(" ").append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), column)).append(" = ");
            sql.append(convertToString(statement.getNewColumnValues().get(column), database));
            sql.append(",");
        }

        sql.deleteCharAt(sql.lastIndexOf(","));
        if (statement.getWhereClause() != null) {
            String fixedWhereClause = "WHERE " + statement.getWhereClause().trim();
            for (Object param : statement.getWhereParameters()) {
                fixedWhereClause = fixedWhereClause.replaceFirst("\\?", database.getDataTypeFactory().fromObject(param, database).objectToString(param, database));
            }
            sql.append(" ").append(fixedWhereClause);
        }

        return new Sql[]{
                new UnparsedSql(sql.toString())
        };
    }

    private String convertToString(Object newValue, Database database) {
        String sqlString;
        if (newValue == null || newValue.toString().equalsIgnoreCase("NULL")) {
            sqlString = "NULL";
        } else if (newValue instanceof String && database.shouldQuoteValue(((String) newValue))) {
            sqlString = "'" + database.escapeStringForDatabase(newValue.toString()) + "'";
        } else if (newValue instanceof Date) {
            // converting java.util.Date to java.sql.Date
            Date date = (Date) newValue;
            if (date.getClass().equals(java.util.Date.class)) {
                date = new java.sql.Date(date.getTime());
            }

            sqlString = database.getDateLiteral(date);
        } else if (newValue instanceof Boolean) {
            if (((Boolean) newValue)) {
                sqlString = database.getDataTypeFactory().getTrueBooleanValue(database);
            } else {
                sqlString = database.getDataTypeFactory().getFalseBooleanValue(database);
            }
        } else {
            sqlString = newValue.toString();
        }
        return sqlString;
    }
}
