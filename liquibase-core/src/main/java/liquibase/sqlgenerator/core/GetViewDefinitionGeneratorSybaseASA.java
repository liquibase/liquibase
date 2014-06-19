package liquibase.sqlgenerator.core;

import liquibase.database.core.SybaseASADatabase;
import liquibase.executor.ExecutionOptions;
import liquibase.action.Sql;
import liquibase.action.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
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
    public Sql[] generateSql(GetViewDefinitionStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[]{
                new UnparsedSql("select viewtext from sysviews where upper(viewname)='" + statement.getViewName().toUpperCase() + "' and upper(vcreator) = '" + statement.getSchemaName().toUpperCase() + '\'')
        };
    }
}