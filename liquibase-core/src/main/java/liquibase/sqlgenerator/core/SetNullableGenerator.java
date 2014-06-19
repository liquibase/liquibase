package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.action.Sql;
import liquibase.action.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.statement.core.SetNullableStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetNullableGenerator extends AbstractSqlGenerator<SetNullableStatement> {

    @Override
    public boolean supports(SetNullableStatement statement, ExecutionOptions options) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        return !(database instanceof FirebirdDatabase ||
                database instanceof SQLiteDatabase);
    }

    @Override
    public ValidationErrors validate(SetNullableStatement setNullableStatement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        validationErrors.checkRequiredField("tableName", setNullableStatement.getTableName());
        validationErrors.checkRequiredField("columnName", setNullableStatement.getColumnName());

        if (database instanceof MSSQLDatabase || database instanceof MySQLDatabase || database instanceof InformixDatabase || database instanceof H2Database) {
            validationErrors.checkRequiredField("columnDataType", setNullableStatement.getColumnDataType());
        }

        try {
            if ((database instanceof DB2Database) && (database.getDatabaseMajorVersion() > 0 && database.getDatabaseMajorVersion() < 9)) {
                validationErrors.addError("DB2 versions less than 9 do not support modifying null constraints");
            }
        } catch (DatabaseException ignore) {
            //cannot check
        }
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(SetNullableStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

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
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + DataTypeFactory.getInstance().fromDescription(statement.getColumnDataType(), database).toDatabaseDataType(database) + nullableString;
        } else if (database instanceof MySQLDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " MODIFY " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + DataTypeFactory.getInstance().fromDescription(statement.getColumnDataType(), database).toDatabaseDataType(database) + nullableString;
        } else if (database instanceof DerbyDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + nullableString;
        } else if (database instanceof HsqlDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " SET"+nullableString;
        } else if (database  instanceof H2Database) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + DataTypeFactory.getInstance().fromDescription(statement.getColumnDataType(), database).toDatabaseDataType(database) + nullableString;
        } else if (database instanceof InformixDatabase) {
            // Informix simply omits the null for nullables
            if (statement.isNullable()) {
                nullableString = "";
            }
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " MODIFY (" + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + DataTypeFactory.getInstance().fromDescription(statement.getColumnDataType(), database).toDatabaseDataType(database) + nullableString + ")";
        } else {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + (statement.isNullable() ? " DROP NOT NULL" : " SET NOT NULL");
        }

        List<Sql> returnList = new ArrayList<Sql>();
        returnList.add(new UnparsedSql(sql));

        if (database instanceof DB2Database) {
            Sql[] a = SqlGeneratorFactory.getInstance().generateSql(new ReorganizeTableStatement(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()), options);
            if (a != null) {
                returnList.addAll(Arrays.asList(a));
            }
        }

        return returnList.toArray(new Sql[returnList.size()]);
    }
}
