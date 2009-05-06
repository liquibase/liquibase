package liquibase.statement.generator;

import liquibase.database.Database;
import liquibase.statement.UpdateStatement;
import liquibase.statement.syntax.Sql;
import liquibase.statement.syntax.UnparsedSql;

import java.util.Date;

public class UpdateGenerator implements SqlGenerator<UpdateStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(UpdateStatement statement, Database database) {
        return true;
    }

    public GeneratorValidationErrors validate(UpdateStatement updateStatement, Database database) {
        return new GeneratorValidationErrors();
    }

    public Sql[] generateSql(UpdateStatement statement, Database database) {
        StringBuffer sql = new StringBuffer("UPDATE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " SET");
        for (String column : statement.getNewColumnValues().keySet()) {
            sql.append(" ").append(database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), column)).append(" = ");
            sql.append(convertToString(statement.getNewColumnValues().get(column), database));
            sql.append(",");
        }

        sql.deleteCharAt(sql.lastIndexOf(","));
        if (statement.getWhereClause() != null) {
            String fixedWhereClause = "WHERE " + statement.getWhereClause();
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
