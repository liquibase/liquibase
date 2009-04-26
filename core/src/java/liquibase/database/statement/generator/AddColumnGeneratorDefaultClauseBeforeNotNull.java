package liquibase.database.statement.generator;

import liquibase.database.statement.AddColumnStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.database.*;
import liquibase.exception.LiquibaseException;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.exception.JDBCException;

public class AddColumnGeneratorDefaultClauseBeforeNotNull extends AddColumnGenerator {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DATABASE_SPECIFIC;
    }

    public boolean isValidGenerator(AddColumnStatement statement, Database database) {
        return database instanceof OracleDatabase
                || database instanceof HsqlDatabase
                || database instanceof DerbyDatabase
                || database instanceof DB2Database
                || database instanceof FirebirdDatabase
                || database instanceof InformixDatabase;
    }

    @Override
    public GeneratorValidationErrors validate(AddColumnStatement statement, Database database) {
        GeneratorValidationErrors validationErrors = super.validate(statement, database);
        if (database instanceof DerbyDatabase && statement.isAutoIncrement()) {
            validationErrors.addError("Cannot add an identity column to a database");
        }
        return validationErrors;
    }

    public Sql[] generateSql(AddColumnStatement statement, Database database) throws JDBCException {
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

        return new Sql[]{
            new UnparsedSql(alterTable)
        };
    }


    private String getDefaultClause(AddColumnStatement statement, Database database) {
        String clause = "";
        if (statement.getDefaultValue() != null) {
           clause += " DEFAULT " + database.convertJavaObjectToString(statement.getDefaultValue());
        }
        return clause;
    }

    private boolean primaryKeyBeforeNotNull(Database database) {
        return !(database instanceof HsqlDatabase || database instanceof H2Database);
    }


}
