package liquibase.database.statement.generator;

import liquibase.database.*;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.database.statement.SetNullableStatement;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class SetNullableGenerator implements SqlGenerator<SetNullableStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(SetNullableStatement statement, Database database) {
        return !(database instanceof FirebirdDatabase ||
                database instanceof SQLiteDatabase);
    }

    public GeneratorValidationErrors validate(SetNullableStatement setNullableStatement, Database database) {
        GeneratorValidationErrors validationErrors = new GeneratorValidationErrors();
        if (database instanceof MSSQLDatabase || database instanceof MySQLDatabase || database instanceof InformixDatabase) {
            validationErrors.checkRequiredField("columnDataType", setNullableStatement.getColumnDataType());
        }
        return validationErrors;
    }

    public Sql[] generateSql(SetNullableStatement statement, Database database) {
        String sql;

        String nullableString;
        if (statement.isNullable()) {
            nullableString = " NULL";
        } else {
            nullableString = " NOT NULL";
        }

        if (database instanceof OracleDatabase || database instanceof SybaseDatabase || database instanceof SybaseASADatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " MODIFY " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + nullableString;
        } else if (database instanceof MSSQLDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + statement.getColumnDataType() + nullableString;
        } else if (database instanceof MySQLDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " MODIFY " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + statement.getColumnDataType() + nullableString;
        } else if (database instanceof DerbyDatabase || database instanceof CacheDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + nullableString;
        } else if (database instanceof HsqlDatabase) {
//            if (statement.getColumnDataType() == null) {
//                throw new StatementNotSupportedOnDatabaseException("Database requires columnDataType parameter", this, database);
//            }
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + statement.getColumnDataType() + nullableString;
        } else if (database instanceof MaxDBDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " COLUMN  " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + (statement.isNullable() ? " DEFAULT NULL" : " NOT NULL");
        } else if (database instanceof InformixDatabase) {
            // Informix simply omits the null for nullables
            if (statement.isNullable()) {
                nullableString = "";
            }
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " MODIFY (" + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + statement.getColumnDataType() + nullableString + ")";
        } else {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + (statement.isNullable() ? " DROP NOT NULL" : " SET NOT NULL");
        }

        return new Sql[] {
                new UnparsedSql(sql)
        };
    }
}
