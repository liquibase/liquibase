package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.core.OracleDatabase;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.GetViewDefinitionStatement;

public class GetViewDefinitionGeneratorOracle extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, ExecutionEnvironment env) {
        return env.getTargetDatabase() instanceof OracleDatabase;
    }

    @Override
    public Action[] generateActions(GetViewDefinitionStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        CatalogAndSchema schema = new CatalogAndSchema(statement.getCatalogName(), statement.getSchemaName()).customize(env.getTargetDatabase());

        return new Action[]{
                new UnparsedSql("SELECT TEXT FROM ALL_VIEWS WHERE upper(VIEW_NAME)='" + statement.getViewName().toUpperCase() + "' AND OWNER='" + schema.getSchemaName() + "'")
        };
    }
}