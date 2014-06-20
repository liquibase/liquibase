package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.action.Action;
import liquibase.action.UnparsedSql;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.core.OracleDatabase;
import liquibase.executor.ExecutionOptions;
import liquibase.statement.core.GetViewDefinitionStatement;

public class GetViewDefinitionGeneratorOracle extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, ExecutionOptions options) {
        return options.getRuntimeEnvironment().getTargetDatabase() instanceof OracleDatabase;
    }

    @Override
    public Action[] generateActions(GetViewDefinitionStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        CatalogAndSchema schema = new CatalogAndSchema(statement.getCatalogName(), statement.getSchemaName()).customize(options.getRuntimeEnvironment().getTargetDatabase());

        return new Action[]{
                new UnparsedSql("SELECT TEXT FROM ALL_VIEWS WHERE upper(VIEW_NAME)='" + statement.getViewName().toUpperCase() + "' AND OWNER='" + schema.getSchemaName() + "'")
        };
    }
}