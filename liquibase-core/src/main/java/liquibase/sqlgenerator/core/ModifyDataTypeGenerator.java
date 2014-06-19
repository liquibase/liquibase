package liquibase.sqlgenerator.core;

import liquibase.datatype.DataTypeFactory;
import liquibase.exception.Warnings;
import liquibase.executor.ExecutionOptions;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.ModifyDataTypeStatement;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;

public class ModifyDataTypeGenerator extends AbstractSqlGenerator<ModifyDataTypeStatement> {

    @Override
    public boolean supports(ModifyDataTypeStatement statement, ExecutionOptions options) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        if (database instanceof SQLiteDatabase) {
            return false;
        }
        return super.supports(statement, options);
    }

    @Override
    public Warnings warn(ModifyDataTypeStatement modifyDataTypeStatement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        Warnings warnings = super.warn(modifyDataTypeStatement, options, sqlGeneratorChain);

        if (database instanceof MySQLDatabase && !modifyDataTypeStatement.getNewDataType().toLowerCase().contains("varchar")) {
            warnings.addWarning("modifyDataType will lose primary key/autoincrement/not null settings for mysql.  Use <sql> and re-specify all configuration if this is the case");
        }

        return warnings;
    }

    @Override
    public ValidationErrors validate(ModifyDataTypeStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", statement.getTableName());
        validationErrors.checkRequiredField("columnName", statement.getColumnName());
        validationErrors.checkRequiredField("newDataType", statement.getNewDataType());

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(ModifyDataTypeStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        String alterTable = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName());

        // add "MODIFY"
        alterTable += " " + getModifyString(database) + " ";

        // add column name
        alterTable += database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName());

        alterTable += getPreDataTypeString(database); // adds a space if nothing else

        // add column type
        alterTable += DataTypeFactory.getInstance().fromDescription(statement.getNewDataType(), database).toDatabaseDataType(database);

        return new Sql[]{new UnparsedSql(alterTable)};
    }

    /**
     * @return either "MODIFY" or "ALTER COLUMN" depending on the current db
     */
    protected String getModifyString(Database database) {
        if (database instanceof SybaseASADatabase
                || database instanceof SybaseDatabase
                || database instanceof MySQLDatabase
                || database instanceof OracleDatabase
                || database instanceof InformixDatabase
                ) {
            return "MODIFY";
        } else {
            return "ALTER COLUMN";
        }
    }

    /**
     * @return the string that comes before the column type
     *         definition (like 'set data type' for derby or an open parentheses for Oracle)
     */
    protected String getPreDataTypeString(Database database) {
        if (database instanceof DerbyDatabase
                || database instanceof DB2Database) {
            return " SET DATA TYPE ";
        } else if (database instanceof SybaseASADatabase
                || database instanceof SybaseDatabase
                || database instanceof MSSQLDatabase
                || database instanceof MySQLDatabase
                || database instanceof HsqlDatabase
                || database instanceof H2Database
                || database instanceof OracleDatabase
                || database instanceof InformixDatabase) {
            return " ";
        } else {
            return " TYPE ";
        }
    }
}
