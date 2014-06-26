package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.RenameColumnStatement;

public class RenameColumnGenerator extends AbstractSqlGenerator<RenameColumnStatement> {

    @Override
    public boolean supports(RenameColumnStatement statement, ExecutionEnvironment env) {
        return !(env.getTargetDatabase() instanceof SQLiteDatabase);
    }

    @Override
    public ValidationErrors validate(RenameColumnStatement renameColumnStatement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", renameColumnStatement.getTableName());
        validationErrors.checkRequiredField("oldColumnName", renameColumnStatement.getOldColumnName());
        validationErrors.checkRequiredField("newColumnName", renameColumnStatement.getNewColumnName());

        if (env.getTargetDatabase() instanceof MySQLDatabase) {
            validationErrors.checkRequiredField("columnDataType", renameColumnStatement.getColumnDataType());
        }

        return validationErrors;
    }

    @Override
    public Action[] generateActions(RenameColumnStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();

        String sql;
        if (database instanceof MSSQLDatabase) {
            // do no escape the new column name. Otherwise it produce "exec sp_rename '[dbo].[person].[usernae]', '[username]'"
            sql = "exec sp_rename '" + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + "." + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getOldColumnName()) + "', '" + statement.getNewColumnName() + "'";
        } else if (database instanceof MySQLDatabase) {
            sql ="ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " CHANGE " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getOldColumnName()) + " " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getNewColumnName()) + " " + DataTypeFactory.getInstance().fromDescription(statement.getColumnDataType(), database).toDatabaseDataType(database);
        } else if (database instanceof SybaseDatabase) {
            sql = "exec sp_rename '" + statement.getTableName() + "." + statement.getOldColumnName() + "', '" + statement.getNewColumnName() + "'";
        } else if (database instanceof HsqlDatabase || database instanceof H2Database) {
            sql ="ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getOldColumnName()) + " RENAME TO " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getNewColumnName());
        } else if (database instanceof FirebirdDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getOldColumnName()) + " TO " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getNewColumnName());
        } else if (
                // supported in Derby from version 10.3.1.4 (see "http://issues.apache.org/jira/browse/DERBY-1490")
                (database instanceof DerbyDatabase)
                || (database instanceof InformixDatabase)) {
          sql = "RENAME COLUMN " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + "." + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getOldColumnName()) + " TO " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getNewColumnName());
        } else if (database instanceof SybaseASADatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " RENAME " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getOldColumnName()) + " TO " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getNewColumnName());
        } else if (database instanceof DB2Database) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " RENAME COLUMN " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getOldColumnName()) + " TO " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getNewColumnName());
        } else {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " RENAME COLUMN " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getOldColumnName()) + " TO " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getNewColumnName());
        }

        if( database instanceof MySQLDatabase && statement.getRemarks() != null ) {
            sql += " COMMENT '" + statement.getRemarks() +"' ";
        }

        return new Action[] {
                new UnparsedSql(sql)
        };
    }
}
