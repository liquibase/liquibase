package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MaxDBDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.GetViewDefinitionStatement;

public class GetViewDefinitionGeneratorMaxDB extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, Database database) {
        return database instanceof MaxDBDatabase;
    }

    @Override
    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[]{
                new UnparsedSql("SELECT DEFINITION FROM DOMAIN.VIEWDEFS WHERE upper(VIEWNAME)='" + statement.getViewName().toUpperCase() + "' AND OWNER='" + database.correctSchemaName(statement.getSchemaName()) + "'")
        };
    }
}