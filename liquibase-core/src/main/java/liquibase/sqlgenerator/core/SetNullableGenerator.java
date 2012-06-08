package liquibase.sqlgenerator.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import liquibase.database.Database;
import liquibase.database.core.CacheDatabase;
import liquibase.database.core.DB2Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MaxDBDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.statement.core.SetNullableStatement;

public class SetNullableGenerator extends AbstractSqlGenerator<SetNullableStatement> {

    @Override
    public boolean supports(SetNullableStatement statement, Database database) {
        return !(database instanceof FirebirdDatabase ||
                database instanceof SQLiteDatabase);
    }

    public ValidationErrors validate(SetNullableStatement setNullableStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("tableName", setNullableStatement.getTableName());
        validationErrors.checkRequiredField("columnName", setNullableStatement.getColumnName());

        if (database instanceof MSSQLDatabase || database instanceof MySQLDatabase || database instanceof InformixDatabase || database instanceof H2Database) {
            validationErrors.checkRequiredField("columnDataType", setNullableStatement.getColumnDataType());
        }

        try {
            if ((database instanceof DB2Database) && (database.getDatabaseMajorVersion() < 9)) {
                validationErrors.addError("DB2 versions less than 9 do not support modifying null constraints");
            }
        } catch (DatabaseException ignore) {
            //cannot check
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
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " MODIFY " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + nullableString;
        } else if (database instanceof MSSQLDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + database.getDataTypeFactory().fromDescription(statement.getColumnDataType()).toDatabaseDataType(database) + nullableString;
        } else if (database instanceof MySQLDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " MODIFY " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + database.getDataTypeFactory().fromDescription(statement.getColumnDataType()).toDatabaseDataType(database) + nullableString;
        } else if (database instanceof DerbyDatabase || database instanceof CacheDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + nullableString;
        } else if (database instanceof HsqlDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " SET"+nullableString;
        } else if (database  instanceof H2Database) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + database.getDataTypeFactory().fromDescription(statement.getColumnDataType()).toDatabaseDataType(database) + nullableString;
        } else if (database instanceof MaxDBDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " COLUMN  " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + (statement.isNullable() ? " DEFAULT NULL" : " NOT NULL");
        } else if (database instanceof InformixDatabase) {
            // Informix simply omits the null for nullables
            if (statement.isNullable()) {
                nullableString = "";
            }
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " MODIFY (" + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + database.getDataTypeFactory().fromDescription(statement.getColumnDataType()).toDatabaseDataType(database) + nullableString + ")";
        } else {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + (statement.isNullable() ? " DROP NOT NULL" : " SET NOT NULL");
        }

        List<Sql> returnList = new ArrayList<Sql>();
        returnList.add(new UnparsedSql(sql));

        if (database instanceof DB2Database) {
            returnList.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(new ReorganizeTableStatement(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()), database)));
        }

        return returnList.toArray(new Sql[returnList.size()]);
    }
}
