package liquibase.sqlgenerator;

import liquibase.statement.GetViewDefinitionStatement;
import liquibase.database.Database;
import liquibase.database.DB2Database;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.exception.JDBCException;
import liquibase.exception.UnexpectedLiquibaseException;

public class GetViewDefinitionGeneratorDB2 extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, Database database) {
        return database instanceof DB2Database;
    }

    @Override
    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database) {
        try {
            return new Sql[] {
                    new UnparsedSql("select view_definition from SYSIBM.VIEWS where TABLE_NAME='" + statement.getViewName() + "' and TABLE_SCHEMA='" + database.convertRequestedSchemaToSchema(statement.getSchemaName()) + "'")
            };
        } catch (JDBCException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }
}
