package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.core.PostgresDatabase;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.GetViewDefinitionStatement;

public class GetViewDefinitionGeneratorPostgres extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, ExecutionEnvironment env) {
        return env.getTargetDatabase() instanceof PostgresDatabase;
    }

    @Override
    public Action[] generateActions(GetViewDefinitionStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        CatalogAndSchema schema = new CatalogAndSchema(statement.getCatalogName(), statement.getSchemaName()).customize(env.getTargetDatabase());

        return new Action[] {
                    new UnparsedSql("select definition from pg_views where viewname='" + statement.getViewName() + "' AND schemaname='" + schema.getSchemaName() + "'" )
            };
    }
}