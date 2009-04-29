package liquibase.database.statement.generator;

import liquibase.database.statement.DropIndexStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.database.Database;
import liquibase.database.MySQLDatabase;
import liquibase.database.MSSQLDatabase;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class DropIndexGenerator implements SqlGenerator<DropIndexStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(DropIndexStatement statement, Database database) {
        return true;
    }

    public GeneratorValidationErrors validate(DropIndexStatement statement, Database database) {
        GeneratorValidationErrors validationErrors = new GeneratorValidationErrors();

        if (database instanceof MySQLDatabase || database instanceof MSSQLDatabase) {
                validationErrors.checkRequiredField("tableName", statement.getTableName());
        }

        return validationErrors;
    }

    public Sql[] generateSql(DropIndexStatement statement, Database database) {
        String schemaName = statement.getTableSchemaName();
        
        if (database instanceof MySQLDatabase) {
            return new Sql[] {new UnparsedSql("DROP INDEX " + database.escapeIndexName(null, statement.getIndexName()) + " ON " + database.escapeTableName(schemaName, statement.getTableName())) };
        } else if (database instanceof MSSQLDatabase) {
            return new Sql[] {new UnparsedSql("DROP INDEX " + database.escapeTableName(schemaName, statement.getTableName()) + "." + database.escapeIndexName(null, statement.getIndexName())) };
        }

        return new Sql[] {new UnparsedSql("DROP INDEX " + database.escapeIndexName(null, statement.getIndexName())) };
    }
}
