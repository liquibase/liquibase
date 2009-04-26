package liquibase.database.statement.generator;

import liquibase.database.statement.RenameTableStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.database.*;
import liquibase.exception.JDBCException;

public class RenameTableGenerator implements SqlGenerator<RenameTableStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(RenameTableStatement statement, Database database) {
        return !(database instanceof CacheDatabase || database instanceof FirebirdDatabase);
    }

    public GeneratorValidationErrors validate(RenameTableStatement renameTableStatement, Database database) {
        return new GeneratorValidationErrors();
    }

    public Sql[] generateSql(RenameTableStatement statement, Database database) throws JDBCException {
        String sql;
        if (database instanceof MSSQLDatabase) {
            sql = "exec sp_rename '" + database.escapeTableName(statement.getSchemaName(), statement.getOldTableName()) + "', " + database.escapeTableName(null, statement.getNewTableName());
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
