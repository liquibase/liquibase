package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.database.core.DB2Database;
import liquibase.executor.ExecutionOptions;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.GetViewDefinitionStatement;

public class GetViewDefinitionGeneratorDB2 extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return SqlGenerator.PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, ExecutionOptions options) {
        return options.getRuntimeEnvironment().getTargetDatabase() instanceof DB2Database;
    }

    @Override
    public Sql[] generateSql(GetViewDefinitionStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        CatalogAndSchema schema = new CatalogAndSchema(statement.getCatalogName(), statement.getSchemaName()).customize(options.getRuntimeEnvironment().getTargetDatabase());

        return new Sql[] {
                    new UnparsedSql("select view_definition from SYSIBM.VIEWS where TABLE_NAME='" + statement.getViewName() + "' and TABLE_SCHEMA='" + schema.getSchemaName() + "'")
            };
    }
}
