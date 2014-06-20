package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.UnparsedSql;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.core.SybaseASADatabase;
import liquibase.executor.ExecutionOptions;
import liquibase.statement.core.GetViewDefinitionStatement;

public class GetViewDefinitionGeneratorSybaseASA extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, ExecutionOptions options) {
        return options.getRuntimeEnvironment().getTargetDatabase() instanceof SybaseASADatabase;
    }

    @Override
    public Action[] generateActions(GetViewDefinitionStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        return new Action[]{
                new UnparsedSql("select viewtext from sysviews where upper(viewname)='" + statement.getViewName().toUpperCase() + "' and upper(vcreator) = '" + statement.getSchemaName().toUpperCase() + '\'')
        };
    }
}