package liquibase.database.statement.generator;

import liquibase.database.statement.InsertStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.database.Database;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.exception.JDBCException;

import java.util.Date;

public class InsertGenerator implements SqlGenerator<InsertStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(InsertStatement statement, Database database) {
        return true;
    }

    public GeneratorValidationErrors validate(InsertStatement insertStatement, Database database) {
        return new GeneratorValidationErrors();
    }

    public Sql[] generateSql(InsertStatement statement, Database database) throws JDBCException {
         if (statement.getSchemaName() != null && !database.supportsSchemas()) {
            throw new StatementNotSupportedOnDatabaseException("Database does not support schemas", statement, database);
        }
        StringBuffer sql = new StringBuffer("INSERT INTO " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " (");
        for (String column : statement.getColumnValues().keySet()) {
            sql.append(database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), column)).append(", ");
        }
        sql.deleteCharAt(sql.lastIndexOf(" "));
        sql.deleteCharAt(sql.lastIndexOf(","));

        sql.append(") VALUES (");

        for (String column : statement.getColumnValues().keySet()) {
            Object newValue = statement.getColumnValues().get(column);
            if (newValue == null || newValue.toString().equals("NULL")) {
                sql.append("NULL");
            } else if (newValue instanceof String && database.shouldQuoteValue(((String) newValue))) {
                sql.append("'").append(database.escapeStringForDatabase((String) newValue)).append("'");
            } else if (newValue instanceof Date) {
                sql.append(database.getDateLiteral(((Date) newValue)));
            } else if (newValue instanceof Boolean) {
                if (((Boolean) newValue)) {
                    sql.append(database.getTrueBooleanValue());
                } else {
                    sql.append(database.getFalseBooleanValue());
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
