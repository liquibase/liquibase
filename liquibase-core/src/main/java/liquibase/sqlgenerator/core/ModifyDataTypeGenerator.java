package liquibase.sqlgenerator.core;

import liquibase.exception.Warnings;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.ModifyDataTypeStatement;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;

public class ModifyDataTypeGenerator extends AbstractSqlGenerator<ModifyDataTypeStatement> {

    @Override
    public Warnings warn(ModifyDataTypeStatement modifyDataTypeStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        Warnings warnings = super.warn(modifyDataTypeStatement, database, sqlGeneratorChain);

        if (database instanceof MySQLDatabase && !modifyDataTypeStatement.getNewDataType().toLowerCase().contains("varchar")) {
            warnings.addWarning("modifyDataType will lose primary key/autoincrement/not null settings for mysql.  Use <sql> and re-specify all configuration if this is the case");
        }

        return warnings;
    }

    public ValidationErrors validate(ModifyDataTypeStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", statement.getTableName());
        validationErrors.checkRequiredField("columnName", statement.getColumnName());
        validationErrors.checkRequiredField("newDataType", statement.getNewDataType());

        return validationErrors;
    }

    public Sql[] generateSql(ModifyDataTypeStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String alterTable = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName());

        // add "MODIFY"
        alterTable += " " + getModifyString(database) + " ";

        // add column name
        alterTable += database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName());

        alterTable += getPreDataTypeString(database); // adds a space if nothing else

        // add column type
        alterTable += TypeConverterFactory.getInstance().findTypeConverter(database).getDataType(statement.getNewDataType(), false);

        return new Sql[]{new UnparsedSql(alterTable)};
    }

    /**
     * @return either "MODIFY" or "ALTER COLUMN" depending on the current db
     */
    private String getModifyString(Database database) {
        if (database instanceof SybaseASADatabase
                || database instanceof SybaseDatabase
                || database instanceof MySQLDatabase
                || database instanceof OracleDatabase
                || database instanceof MaxDBDatabase
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
    private String getPreDataTypeString(Database database) {
        if (database instanceof DerbyDatabase
                || database instanceof DB2Database) {
            return " SET DATA TYPE ";
        } else if (database instanceof SybaseASADatabase
                || database instanceof SybaseDatabase
                || database instanceof MSSQLDatabase
                || database instanceof MySQLDatabase
                || database instanceof HsqlDatabase
                || database instanceof H2Database
                || database instanceof CacheDatabase
                || database instanceof OracleDatabase
                || database instanceof MaxDBDatabase) {
            return " ";
        } else {
            return " TYPE ";
        }
    }
}
