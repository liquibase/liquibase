package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.database.core.MSSQLDatabase;
import liquibase.executor.ExecutionOptions;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.GetViewDefinitionStatement;

public class GetViewDefinitionGeneratorMSSQL extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, ExecutionOptions options) {
        return options.getRuntimeEnvironment().getTargetDatabase() instanceof MSSQLDatabase;
    }

    @Override
    public Sql[] generateSql(GetViewDefinitionStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        CatalogAndSchema schema = new CatalogAndSchema(statement.getCatalogName(), statement.getSchemaName()).customize(options.getRuntimeEnvironment().getTargetDatabase());

        String sql = "exec sp_helptext '" + schema.getSchemaName() + "."+ statement.getViewName() + "'";
            return new Sql[]{new UnparsedSql(sql) };
    }}