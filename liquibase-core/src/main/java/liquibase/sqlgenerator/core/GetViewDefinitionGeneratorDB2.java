package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.structure.core.Schema;
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
    public boolean supports(GetViewDefinitionStatement statement, Database database) {
        return database instanceof DB2Database;
    }

    @Override
    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        CatalogAndSchema schema = database.correctSchema(new CatalogAndSchema(statement.getCatalogName(), statement.getSchemaName()));

        return new Sql[] {
                    new UnparsedSql("select view_definition from SYSIBM.VIEWS where TABLE_NAME='" + statement.getViewName() + "' and TABLE_SCHEMA='" + schema.getSchemaName() + "'")
            };
    }
}
