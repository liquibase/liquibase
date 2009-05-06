package liquibase.statement.generator;

import liquibase.database.Database;
import liquibase.database.SQLiteDatabase;
import liquibase.statement.DeleteStatement;
import liquibase.statement.syntax.Sql;
import liquibase.statement.syntax.UnparsedSql;
import liquibase.exception.ValidationErrors;
import liquibase.exception.ValidationErrors;

import java.util.Date;

public class DeleteGenerator implements SqlGenerator<DeleteStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(DeleteStatement statement, Database database) {
        return !(database instanceof SQLiteDatabase);
    }

    public ValidationErrors validate(DeleteStatement deleteStatement, Database database) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(DeleteStatement statement, Database database) {
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
