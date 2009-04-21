package liquibase.database.sql.generator;

import liquibase.database.*;
import liquibase.database.sql.AddColumnStatement;
import liquibase.exception.LiquibaseException;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class AddColumnGeneratorDefault implements SqlGenerator<AddColumnStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValid(AddColumnStatement statement, Database database) {
        return true;
    }

    public String[] generateSql(AddColumnStatement statement, Database database) throws LiquibaseException {

        if (statement.isPrimaryKey() && (database instanceof CacheDatabase
                || database instanceof H2Database
                || database instanceof DB2Database
                || database instanceof DerbyDatabase
                || database instanceof SQLiteDatabase)) {
            throw new StatementNotSupportedOnDatabaseException("Adding primary key columns is not supported", statement, database);
        }

        String alterTable = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ADD " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + database.getColumnType(statement.getColumnType(), statement.isAutoIncrement());

        if (statement.isAutoIncrement()) {
            alterTable += " " + database.getAutoIncrementClause();
        }

        if (!statement.isNullable()) {
            alterTable += " NOT NULL";
        } else {
            if (database instanceof SybaseDatabase || database instanceof SybaseASADatabase) {
                alterTable += " NULL";
            }
        }

        if (statement.isPrimaryKey()) {
            alterTable += " PRIMARY KEY";
        }

        alterTable += getDefaultClause(statement, database);

        return new String[]{
                alterTable
        };
    }

    private String getDefaultClause(AddColumnStatement statement, Database database) {
        String clause = "";
        if (statement.getDefaultValue() != null) {
            if (database instanceof MSSQLDatabase) {
                clause += " CONSTRAINT " + ((MSSQLDatabase) database).generateDefaultConstraintName(statement.getTableName(), statement.getColumnName());
            }
            clause += " DEFAULT " + database.convertJavaObjectToString(statement.getDefaultValue());
        }
        return clause;
    }

}
