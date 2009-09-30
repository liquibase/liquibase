package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.SetNullableStatement;
import liquibase.statement.core.ReorganizeTableStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class SetNullableGenerator implements SqlGenerator<SetNullableStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(SetNullableStatement statement, Database database) {
        return !(database instanceof FirebirdDatabase ||
                database instanceof SQLiteDatabase);
    }

    public ValidationErrors validate(SetNullableStatement setNullableStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("tableName", setNullableStatement.getTableName());
        validationErrors.checkRequiredField("columnName", setNullableStatement.getColumnName());

        if (database instanceof MSSQLDatabase || database instanceof MySQLDatabase || database instanceof InformixDatabase) {
            validationErrors.checkRequiredField("columnDataType", setNullableStatement.getColumnDataType());
        }
        return validationErrors;
    }

    public Sql[] generateSql(SetNullableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
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
        } else if (database instanceof HsqlDatabase || database  instanceof H2Database) {
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

        List<Sql> returnList = new ArrayList<Sql>();
        returnList.add(new UnparsedSql(sql));

        if (database instanceof DB2Database) {
            returnList.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(new ReorganizeTableStatement(statement.getSchemaName(), statement.getTableName()), database)));
        }

        return returnList.toArray(new Sql[returnList.size()]);
    }
}
