package liquibase.sqlgenerator.core;

import liquibase.statement.GetViewDefinitionStatement;
import liquibase.database.*;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.exception.JDBCException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sqlgenerator.SqlGeneratorChain;

public class GetViewDefinitionGeneratorHsql extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, Database database) {
        return database instanceof HsqlDatabase && !(database instanceof H2Database);
    }

    @Override
    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        try {
            return new Sql[] {
                    new UnparsedSql("SELECT VIEW_DEFINITION FROM INFORMATION_SCHEMA.SYSTEM_VIEWS WHERE TABLE_NAME = '" + statement.getViewName() + "' AND TABLE_SCHEMA='" + database.convertRequestedSchemaToSchema(statement.getSchemaName()) + "'")
            };
        } catch (JDBCException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }
}