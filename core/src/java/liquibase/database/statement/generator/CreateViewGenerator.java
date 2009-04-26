package liquibase.database.statement.generator;

import liquibase.database.*;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.database.statement.CreateViewStatement;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.exception.JDBCException;

public class CreateViewGenerator implements SqlGenerator<CreateViewStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(CreateViewStatement statement, Database database) {
        return true;
    }

    public GeneratorValidationErrors validate(CreateViewStatement createViewStatement, Database database) {
        return new GeneratorValidationErrors();
    }

    public Sql[] generateSql(CreateViewStatement statement, Database database) throws JDBCException {
        String createClause;
        if (database instanceof HsqlDatabase
                || database instanceof DB2Database
                || database instanceof CacheDatabase
                || database instanceof MSSQLDatabase
                || database instanceof DerbyDatabase
                || database instanceof SybaseASADatabase
                || database instanceof InformixDatabase) {
            if (statement.isReplaceIfExists()) {
                throw new StatementNotSupportedOnDatabaseException("replaceIfExists not supported", statement, database);
            }
        }


        if (database instanceof FirebirdDatabase) {
            if (statement.isReplaceIfExists()) {
                createClause = "RECREATE VIEW";
            } else {
                createClause = "RECREATE VIEW";
            }
        } else if (database instanceof SybaseASADatabase && statement.getSelectQuery().toLowerCase().startsWith("create view")) {
            // Sybase ASA saves view definitions with header.
            return new Sql[]{
                    new UnparsedSql(statement.getSelectQuery())
            };
        } else {
            createClause = "CREATE " + (statement.isReplaceIfExists() ? "OR REPLACE " : "") + "VIEW";
        }

        return new Sql[]{
                new UnparsedSql(createClause + " " + database.escapeViewName(statement.getSchemaName(), statement.getViewName()) + " AS " + statement.getSelectQuery())
        };
    }
}
