package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DeleteStatement;

import java.util.Date;

public class DeleteGenerator extends AbstractSqlGenerator<DeleteStatement> {

    @Override
    public boolean supports(DeleteStatement statement, Database database) {
        return !(database instanceof SQLiteDatabase);
    }

    public ValidationErrors validate(DeleteStatement deleteStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", deleteStatement.getTableName());
        return validationErrors;
    }

    public Sql[] generateSql(DeleteStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuffer sql = new StringBuffer("DELETE FROM " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()));

        if (statement.getWhereClause() != null) {
            String fixedWhereClause = " WHERE " + statement.getWhereClause();
            for (Object param : statement.getWhereParameters()) {
                fixedWhereClause = fixedWhereClause.replaceFirst("\\?", convertToString(param, database));
            }
            sql.append(" ").append(fixedWhereClause);
        }

        return new Sql[]{new UnparsedSql(sql.toString())};
    }

    private String convertToString(Object newValue, Database database) {
        String sqlString;
        if (newValue == null) {
            sqlString = "NULL";
        } else if (newValue instanceof String && database.shouldQuoteValue(((String) newValue))) {
            sqlString = "'" + newValue + "'";
        } else if (newValue instanceof Date) {
            sqlString = database.getDateLiteral(((Date) newValue));
        } else if (newValue instanceof Boolean) {
            if (((Boolean) newValue)) {
                sqlString = TypeConverterFactory.getInstance().findTypeConverter(database).getBooleanType().getTrueBooleanValue();
            } else {
                sqlString = TypeConverterFactory.getInstance().findTypeConverter(database).getBooleanType().getFalseBooleanValue();
            }
        } else {
            sqlString = newValue.toString();
        }
        return sqlString;
    }
}
