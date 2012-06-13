package liquibase.sqlgenerator.core;

import java.util.Date;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.InsertStatement;

public class InsertGenerator extends AbstractSqlGenerator<InsertStatement> {

    public ValidationErrors validate(InsertStatement insertStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", insertStatement.getTableName());
        validationErrors.checkRequiredField("columns", insertStatement.getColumnValues());

        if (insertStatement.getSchemaName() != null && !database.supportsSchemas()) {
           validationErrors.addError("Database does not support schemas");
       }

        return validationErrors;
    }

    public Sql[] generateSql(InsertStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
    	   	
        StringBuffer sql = new StringBuffer("INSERT INTO " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " (");
        for (String column : statement.getColumnValues().keySet()) {
            sql.append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), column)).append(", ");
        }
        sql.deleteCharAt(sql.lastIndexOf(" "));
        sql.deleteCharAt(sql.lastIndexOf(","));

        sql.append(") VALUES (");

        for (String column : statement.getColumnValues().keySet()) {
            Object newValue = statement.getColumnValues().get(column);
            if (newValue == null || newValue.toString().equalsIgnoreCase("NULL")) {
                sql.append("NULL");
            } else if (newValue instanceof String && database.shouldQuoteValue(((String) newValue))) {
                sql.append("'").append(database.escapeStringForDatabase((String) newValue)).append("'");
            } else if (newValue instanceof Date) {
                sql.append(database.getDateLiteral(((Date) newValue)));
            } else if (newValue instanceof Boolean) {
                if (((Boolean) newValue)) {
                    sql.append(database.getDataTypeFactory().getTrueBooleanValue(database));
                } else {
                    sql.append(database.getDataTypeFactory().getFalseBooleanValue(database));
                }
            } else {
                sql.append(newValue);
            }
            sql.append(", ");
        }

        sql.deleteCharAt(sql.lastIndexOf(" "));
        sql.deleteCharAt(sql.lastIndexOf(","));

        sql.append(")");

        return new Sql[] {
                new UnparsedSql(sql.toString())
        };
    }
}
