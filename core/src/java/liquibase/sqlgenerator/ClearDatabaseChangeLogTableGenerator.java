package liquibase.sqlgenerator;

import liquibase.statement.ClearDatabaseChangeLogTableStatement;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.exception.JDBCException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;

public class ClearDatabaseChangeLogTableGenerator implements SqlGenerator<ClearDatabaseChangeLogTableStatement> {
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(ClearDatabaseChangeLogTableStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(ClearDatabaseChangeLogTableStatement clearDatabaseChangeLogTableStatement, Database database) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(ClearDatabaseChangeLogTableStatement statement, Database database) {
        try {
            return new Sql[] {
                    new UnparsedSql("DELETE FROM " + database.escapeTableName(database.convertRequestedSchemaToSchema(null), database.getDatabaseChangeLogTableName()))
            };
        } catch (JDBCException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }
}
