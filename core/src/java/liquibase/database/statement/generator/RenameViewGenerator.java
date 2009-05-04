package liquibase.database.statement.generator;

import liquibase.database.statement.RenameViewStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.database.*;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

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
                || database instanceof InformixDatabase
                || database instanceof SybaseASADatabase);
    }

    public GeneratorValidationErrors validate(RenameViewStatement renameViewStatement, Database database) {
        GeneratorValidationErrors validationErrors = new GeneratorValidationErrors();
        if (database instanceof OracleDatabase) {
            validationErrors.checkDisallowedField("schemaName", renameViewStatement.getSchemaName());
        }

        return validationErrors;
    }

    public Sql[] generateSql(RenameViewStatement statement, Database database) {
        String sql;

        if (database instanceof MSSQLDatabase) {
            sql = "exec sp_rename '" + database.escapeViewName(statement.getSchemaName(), statement.getOldViewName()) + "', '" + statement.getNewViewName() + '\'';
        } else if (database instanceof MySQLDatabase) {
            sql = "RENAME TABLE " + database.escapeViewName(statement.getSchemaName(), statement.getOldViewName()) + " TO " + database.escapeViewName(statement.getSchemaName(), statement.getNewViewName());
        } else if (database instanceof PostgresDatabase) {
            sql = "ALTER TABLE " + database.escapeViewName(statement.getSchemaName(), statement.getOldViewName()) + " RENAME TO " + database.escapeViewName(null, statement.getNewViewName());
        } else if (database instanceof MaxDBDatabase) {
            sql = "RENAME VIEW " + database.escapeViewName(statement.getSchemaName(), statement.getOldViewName()) + " TO " + database.escapeViewName(null, statement.getNewViewName());
        } else {
            sql = "RENAME " + database.escapeViewName(statement.getSchemaName(), statement.getOldViewName()) + " TO " + database.escapeViewName(null, statement.getNewViewName());
        }

        return new Sql[]{
                new UnparsedSql(sql)
        };
    }
}
