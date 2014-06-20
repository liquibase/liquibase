package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.UnparsedSql;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.core.InformixDatabase;
import liquibase.executor.ExecutionOptions;
import liquibase.statement.core.GetViewDefinitionStatement;

public class GetViewDefinitionGeneratorInformix extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, ExecutionOptions options) {
        return options.getRuntimeEnvironment().getTargetDatabase() instanceof InformixDatabase;
    }

    @Override
    public Action[] generateActions(GetViewDefinitionStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        // TODO owner is schemaName ?
        // view definition is distributed over multiple rows, each 64 chars
    	// see InformixDatabase.getViewDefinition
        return new Action[]{
                new UnparsedSql("select v.viewtext from sysviews v, systables t where t.tabname = '" + statement.getViewName() + "' and v.tabid = t.tabid and t.tabtype = 'V' order by v.seqno")
        };
    }
}