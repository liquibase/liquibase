package liquibase.statement.generator;

import liquibase.database.*;
import liquibase.statement.RenameTableStatement;
import liquibase.statement.syntax.Sql;
import liquibase.statement.syntax.UnparsedSql;
import liquibase.exception.ValidationErrors;
import liquibase.exception.ValidationErrors;

public class RenameTableGenerator implements SqlGenerator<RenameTableStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(RenameTableStatement statement, Database database) {
        return !(database instanceof CacheDatabase || database instanceof FirebirdDatabase);
    }

    public ValidationErrors validate(RenameTableStatement renameTableStatement, Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("schemaName", renameTableStatement.getSchemaName());
        validationErrors.checkRequiredField("newTableName", renameTableStatement.getNewTableName());
        validationErrors.checkRequiredField("oldTableName", renameTableStatement.getOldTableName());
        return validationErrors;
    }

    public Sql[] generateSql(RenameTableStatement statement, Database database) {
        String sql;
        if (database instanceof MSSQLDatabase) {
            sql = "exec sp_rename '" + database.escapeTableName(statement.getSchemaName(), statement.getOldTableName()) + "', '" + statement.getNewTableName() + '\'';
        } else if (database instanceof MySQLDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getOldTableName()) + " RENAME " + database.escapeTableName(statement.getSchemaName(), statement.getNewTableName());
        } else if (database instanceof PostgresDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getOldTableName()) + " RENAME TO " + database.escapeTableName(null, statement.getNewTableName());
        } else if (database instanceof SybaseASADatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getOldTableName()) + " RENAME " + database.escapeTableName(null, statement.getNewTableName());
        } else if ((database instanceof DerbyDatabase) || (database instanceof MaxDBDatabase) || (database instanceof InformixDatabase)) {
            sql = "RENAME TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getOldTableName()) + " TO " + database.escapeTableName(null, statement.getNewTableName());
        } else if (database instanceof HsqlDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getOldTableName()) + " RENAME TO " + database.escapeTableName(null, statement.getNewTableName());
        } else if (database instanceof OracleDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getOldTableName()) + " RENAME TO " + database.escapeTableName(null, statement.getNewTableName());
        } else if (database instanceof DB2Database) {
            sql = "RENAME " + database.escapeTableName(statement.getSchemaName(), statement.getOldTableName()) + " TO " + database.escapeTableName(null, statement.getNewTableName());//db2 doesn't allow specifying new schema name
        } else if (database instanceof SQLiteDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getOldTableName()) + " RENAME TO " + database.escapeTableName(null, statement.getNewTableName());
        } else {
            sql = "RENAME " + database.escapeTableName(statement.getSchemaName(), statement.getOldTableName()) + " TO " + database.escapeTableName(statement.getSchemaName(), statement.getNewTableName());
        }

        return new Sql[] {
                new UnparsedSql(sql)
        };  //To change body of implemented methods use File | Settings | File Templates.
    }
}
