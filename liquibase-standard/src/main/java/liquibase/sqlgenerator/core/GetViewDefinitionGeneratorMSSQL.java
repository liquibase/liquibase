package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.GetViewDefinitionStatement;
import liquibase.structure.core.View;

public class GetViewDefinitionGeneratorMSSQL extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, Database database) {
        return database instanceof MSSQLDatabase;
    }

    @Override
    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        CatalogAndSchema schema = new CatalogAndSchema(statement.getCatalogName(), statement.getSchemaName()).customize(database);
        String viewNameEscaped = database.escapeObjectName(schema.getCatalogName(), schema.getSchemaName(), statement.getViewName(), View.class);
        String sql;
        sql = "SELECT OBJECT_DEFINITION(OBJECT_ID(N'" + database.escapeStringForDatabase(viewNameEscaped) + "')) AS [ObjectDefinition]";
        return new Sql[] { new UnparsedSql(sql) };
    }
}