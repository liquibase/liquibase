package liquibase.database.statement.generator;

import liquibase.database.statement.RenameViewStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.database.*;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.exception.JDBCException;

public class RenameViewGenerator implements SqlGenerator<RenameViewStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(RenameViewStatement statement, Database database) {
        return !(database instanceof DerbyDatabase
                || database instanceof HsqlDatabase
                || database instanceof DB2Database
                || database instanceof CacheDatabase
                || database instanceof FirebirdDatabase
                || database instanceof InformixDatabase);
    }

    public GeneratorValidationErrors validate(RenameViewStatement renameViewStatement, Database database) {
        return new GeneratorValidationErrors();
    }

    public Sql[] generateSql(RenameViewStatement statement, Database database) throws JDBCException {
        String sql;

        if (database instanceof MSSQLDatabase) {
            sql = "exec sp_rename '" + database.escapeViewName(statement.getSchemaName(), statement.getOldViewName()) + "', " + database.escapeViewName(null, statement.getNewViewName());
        } else if (database instanceof MySQLDatabase) {
            sql = "RENAME TABLE " + database.escapeViewName(statement.getSchemaName(), statement.getOldViewName()) + " TO " + database.escapeViewName(statement.getSchemaName(), statement.getNewViewName());
        } else if (database instanceof PostgresDatabase) {
            sql = "ALTER TABLE " + database.escapeViewName(statement.getSchemaName(), statement.getOldViewName()) + " RENAME TO " + database.escapeViewName(null, statement.getNewViewName());
        } else if (database instanceof MaxDBDatabase) {
            sql = "RENAME VIEW " + database.escapeViewName(statement.getSchemaName(), statement.getOldViewName()) + " TO " + database.escapeViewName(null, statement.getNewViewName());
        } else {
            if (statement.getSchemaName() != null && database instanceof OracleDatabase) {
                throw new StatementNotSupportedOnDatabaseException("Cannot specify schema when renaming in oracle", statement, database);
            }

            if (database instanceof SybaseASADatabase) {
                throw new StatementNotSupportedOnDatabaseException("Sybase ASA does not support renaming of view. Please drop old view and create a new one manually.", statement, database);

            }
            sql = "RENAME " + database.escapeViewName(statement.getSchemaName(), statement.getOldViewName()) + " TO " + database.escapeViewName(null, statement.getNewViewName());

        }

        return new Sql[]{
                new UnparsedSql(sql)
        };
    }
}
