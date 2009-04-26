package liquibase.database.statement.generator;

import liquibase.database.statement.DropIndexStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.database.Database;
import liquibase.database.MySQLDatabase;
import liquibase.database.MSSQLDatabase;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.exception.JDBCException;

public class DropIndexGenerator implements SqlGenerator<DropIndexStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(DropIndexStatement statement, Database database) {
        return true;
    }

    public GeneratorValidationErrors validate(DropIndexStatement dropIndexStatement, Database database) {
        return new GeneratorValidationErrors();
    }

    public Sql[] generateSql(DropIndexStatement statement, Database database) throws JDBCException {
        String schemaName = statement.getTableSchemaName();
        
        if (database instanceof MySQLDatabase) {
            if (statement.getTableName() == null) {
                throw new StatementNotSupportedOnDatabaseException("tableName is required", statement, database);
            }
            return new Sql[] {new UnparsedSql("DROP INDEX " + database.escapeIndexName(null, statement.getIndexName()) + " ON " + database.escapeTableName(schemaName, statement.getTableName())) };
        } else if (database instanceof MSSQLDatabase) {
            if (statement.getTableName() == null) {
                throw new StatementNotSupportedOnDatabaseException("tableName is required", statement, database);
            }
            return new Sql[] {new UnparsedSql("DROP INDEX " + database.escapeTableName(schemaName, statement.getTableName()) + "." + database.escapeIndexName(null, statement.getIndexName())) };
        }

        return new Sql[] {new UnparsedSql("DROP INDEX " + database.escapeIndexName(null, statement.getIndexName())) };
    }
}
