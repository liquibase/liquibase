package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.UpdateStatement;

import java.util.Date;

public class UpdateGenerator implements SqlGenerator<UpdateStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(UpdateStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(UpdateStatement updateStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", updateStatement.getTableName());
        validationErrors.checkRequiredField("columns", updateStatement.getNewColumnValues());
        return validationErrors;
    }

    public Sql[] generateSql(UpdateStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuffer sql = new StringBuffer("UPDATE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " SET");
        for (String column : statement.getNewColumnValues().keySet()) {
            sql.append(" ").append(database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), column)).append(" = ");
            sql.append(convertToString(statement.getNewColumnValues().get(column), database));
            sql.append(",");
        }

        sql.deleteCharAt(sql.lastIndexOf(","));
        if (statement.getWhereClause() != null) {
            String fixedWhereClause = "WHERE " + statement.getWhereClause().trim();
            for (Object param : statement.getWhereParameters()) {
                fixedWhereClause = fixedWhereClause.replaceFirst("\\?", convertToString(param, database));
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
            sqlString = database.getDateLiteral(((Date) newValue));
        } else if (newValue instanceof Boolean) {
            if (((Boolean) newValue)) {
                sqlString = database.getTrueBooleanValue();
            } else {
                sqlString = database.getFalseBooleanValue();
            }
        } else {
            sqlString = newValue.toString();
        }
        return sqlString;
    }
}
