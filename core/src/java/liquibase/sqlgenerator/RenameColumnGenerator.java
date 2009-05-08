package liquibase.sqlgenerator;

import liquibase.database.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.RenameColumnStatement;

public class RenameColumnGenerator implements SqlGenerator<RenameColumnStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(RenameColumnStatement statement, Database database) {
        return !(database instanceof DB2Database
                || database instanceof CacheDatabase
                || database instanceof SQLiteDatabase);
    }

    public ValidationErrors validate(RenameColumnStatement renameColumnStatement, Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", renameColumnStatement.getTableName());
        validationErrors.checkRequiredField("oldColumnName", renameColumnStatement.getOldColumnName());
        validationErrors.checkRequiredField("newColumnName", renameColumnStatement.getNewColumnName());

        if (database instanceof MySQLDatabase) {
            validationErrors.checkRequiredField("columnDataType", renameColumnStatement.getColumnDataType());
        }

        return validationErrors;
    }

    public Sql[] generateSql(RenameColumnStatement statement, Database database) {
        String sql;
        if (database instanceof MSSQLDatabase) {
        	// do no escape the new column name. Otherwise it produce "exec sp_rename '[dbo].[person].[usernae]', '[username]'"
            sql = "exec sp_rename '" + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + "." + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getOldColumnName()) + "', '" + statement.getNewColumnName() + "'";
        } else if (database instanceof MySQLDatabase) {
            sql ="ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " CHANGE " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getOldColumnName()) + " " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getNewColumnName()) + " " + statement.getColumnDataType();
        } else if (database instanceof HsqlDatabase) {
            sql ="ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getOldColumnName()) + " RENAME TO " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getNewColumnName());
        } else if (database instanceof FirebirdDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getOldColumnName()) + " TO " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getNewColumnName());
        } else if ((database instanceof MaxDBDatabase)
                // supported in Derby from version 10.3.1.4 (see "http://issues.apache.org/jira/browse/DERBY-1490")
                || (database instanceof DerbyDatabase)
                || (database instanceof InformixDatabase)) {
          sql = "RENAME COLUMN " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + "." + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getOldColumnName()) + " TO " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getNewColumnName());
        } else {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " RENAME COLUMN " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getOldColumnName()) + " TO " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getNewColumnName());
        }

        return new Sql[] {
                new UnparsedSql(sql)
        };
    }
}
