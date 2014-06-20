package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.action.Action;
import liquibase.action.UnparsedSql;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.core.PostgresDatabase;
import liquibase.executor.ExecutionOptions;
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
    public Action[] generateActions(GetViewDefinitionStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        CatalogAndSchema schema = new CatalogAndSchema(statement.getCatalogName(), statement.getSchemaName()).customize(options.getRuntimeEnvironment().getTargetDatabase());

        return new Action[] {
                    new UnparsedSql("select definition from pg_views where viewname='" + statement.getViewName() + "' AND schemaname='" + schema.getSchemaName() + "'" )
            };
    }
}