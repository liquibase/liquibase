package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.core.MSSQLDatabase;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.GetViewDefinitionStatement;

public class GetViewDefinitionGeneratorMSSQL extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, ExecutionEnvironment env) {
        return env.getTargetDatabase() instanceof MSSQLDatabase;
    }

    @Override
    public Action[] generateActions(GetViewDefinitionStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        CatalogAndSchema schema = new CatalogAndSchema(statement.getCatalogName(), statement.getSchemaName()).customize(env.getTargetDatabase());

        String sql = "exec sp_helptext '" + schema.getSchemaName() + "."+ statement.getViewName() + "'";
            return new Action[]{new UnparsedSql(sql) };
    }}