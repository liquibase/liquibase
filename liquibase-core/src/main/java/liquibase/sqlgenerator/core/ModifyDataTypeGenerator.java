package liquibase.sqlgenerator.core;

import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.DatabaseDataType;
import liquibase.exception.Warnings;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.ModifyDataTypeStatement;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;

public class ModifyDataTypeGenerator extends AbstractSqlGenerator<ModifyDataTypeStatement> {

    @Override
    public boolean supports(ModifyDataTypeStatement statement, Database database) {
        if (database instanceof SQLiteDatabase) {
            return false;
        }
        return super.supports(statement, database);
    }

    @Override
    public Warnings warn(ModifyDataTypeStatement modifyDataTypeStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        Warnings warnings = super.warn(modifyDataTypeStatement, database, sqlGeneratorChain);

        if (database instanceof MySQLDatabase && !modifyDataTypeStatement.getNewDataType().toLowerCase().contains("varchar")) {
            warnings.addWarning("modifyDataType will lose primary key/autoincrement/not null settings for mysql.  Use <sql> and re-specify all configuration if this is the case");
        }

        return warnings;
    }

    @Override
    public ValidationErrors validate(ModifyDataTypeStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", statement.getTableName());
        validationErrors.checkRequiredField("columnName", statement.getColumnName());
        validationErrors.checkRequiredField("newDataType", statement.getNewDataType());

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(ModifyDataTypeStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String alterTable = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName());

        // add "MODIFY"
        alterTable += " " + getModifyString(database) + " ";

        // add column name
        String columnName = database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName());
        alterTable += columnName;

        alterTable += getPreDataTypeString(database); // adds a space if nothing else

        // add column type
        DatabaseDataType newDataType = DataTypeFactory.getInstance().fromDescription(statement.getNewDataType(), database).toDatabaseDataType(database);

        alterTable += newDataType;

        if (database instanceof PostgresDatabase) {
            alterTable += " USING ("+columnName+"::"+newDataType+")";
        }

        return new Sql[]{new UnparsedSql(alterTable, getAffectedTable(statement))};
    }

    protected Relation getAffectedTable(ModifyDataTypeStatement statement) {
        return new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
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
