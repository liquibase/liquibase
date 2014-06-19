package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.database.core.PostgresDatabase;
import liquibase.executor.ExecutionOptions;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.GetViewDefinitionStatement;

public class GetViewDefinitionGeneratorPostgres extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, ExecutionOptions options) {
        return options.getRuntimeEnvironment().getTargetDatabase() instanceof PostgresDatabase;
    }

    @Override
    public Sql[] generateSql(GetViewDefinitionStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        CatalogAndSchema schema = new CatalogAndSchema(statement.getCatalogName(), statement.getSchemaName()).customize(options.getRuntimeEnvironment().getTargetDatabase());

        return new Sql[] {
                    new UnparsedSql("select definition from pg_views where viewname='" + statement.getViewName() + "' AND schemaname='" + schema.getSchemaName() + "'" )
            };
    }
}