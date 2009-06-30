package liquibase.sqlgenerator.core;

import liquibase.statement.core.GetViewDefinitionStatement;
import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.MaxDBDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.exception.JDBCException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sqlgenerator.SqlGeneratorChain;

public class GetViewDefinitionGeneratorOracle extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, Database database) {
        return database instanceof OracleDatabase;
    }

    @Override
    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        try {
            return new Sql[] {
                    new UnparsedSql("SELECT TEXT FROM ALL_VIEWS WHERE upper(VIEW_NAME)='"+statement.getViewName().toUpperCase()+"' AND OWNER='"+database.convertRequestedSchemaToSchema(statement.getSchemaName())+"'" )
            };
        } catch (JDBCException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }
}