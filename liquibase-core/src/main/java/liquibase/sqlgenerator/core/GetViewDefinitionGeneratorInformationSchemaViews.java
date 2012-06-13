package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.GetViewDefinitionStatement;

public class GetViewDefinitionGeneratorInformationSchemaViews extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, Database database) {
        return database instanceof H2Database || database instanceof MySQLDatabase;
    }

    @Override
    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String schema;
        if (database instanceof MySQLDatabase) {
            schema = database.correctCatalogName(statement.getCatalogName());
        } else {
            schema = database.correctSchemaName(statement.getSchemaName());
        }
        return new Sql[]{
                new UnparsedSql("SELECT VIEW_DEFINITION FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_NAME = '" + statement.getViewName() + "' AND TABLE_SCHEMA='" + schema + "'")
        };
    }
}