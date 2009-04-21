package liquibase.database.statement.generator;

import liquibase.database.statement.AddColumnStatement;
import liquibase.database.*;
import liquibase.exception.LiquibaseException;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class AddColumnGeneratorDefaultClauseBeforeNotNull implements SqlGenerator<AddColumnStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DATABASE_SPECIFIC;
    }

    public boolean isValid(AddColumnStatement statement, Database database) {
        return database instanceof OracleDatabase
                || database instanceof HsqlDatabase
                || database instanceof DerbyDatabase
                || database instanceof DB2Database
                || database instanceof FirebirdDatabase
                || database instanceof InformixDatabase;
    }

    public String[] generateSql(AddColumnStatement statement, Database database) throws LiquibaseException {
        if (statement.isPrimaryKey()
                && (database instanceof H2Database || database instanceof DB2Database || database instanceof DerbyDatabase)) {
            throw new StatementNotSupportedOnDatabaseException("Adding primary key columns is not supported", statement, database);
        }

        String alterTable = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ADD " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + database.getColumnType(statement.getColumnType(), statement.isAutoIncrement());

        alterTable += getDefaultClause(statement, database);

        if (primaryKeyBeforeNotNull(database)) {
            if (statement.isPrimaryKey()) {
                alterTable += " PRIMARY KEY";
            }
        }

        if (statement.isAutoIncrement()) {
            alterTable += " " + database.getAutoIncrementClause();
        }

        if (!statement.isNullable()) {
            alterTable += " NOT NULL";
        }

        if (!primaryKeyBeforeNotNull(database)) {
            if (statement.isPrimaryKey()) {
                alterTable += " PRIMARY KEY";
            }
        }

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

    private boolean primaryKeyBeforeNotNull(Database database) {
        return !(database instanceof HsqlDatabase);
    }


}
