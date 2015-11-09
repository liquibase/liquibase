package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.GetViewDefinitionStatement;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.View;

public class GetViewDefinitionGeneratorMSSQL extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, Database database) {
        return false; //todo: action refactoring database instanceof MSSQLDatabase;
    }

    @Override
    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        CatalogAndSchema schema = new CatalogAndSchema(statement.getCatalogName(), statement.getSchemaName()).customize(database);
        boolean sql2005OrLater = true;
        try {
            sql2005OrLater = database.getDatabaseMajorVersion() >= 9;
        } catch (Exception ignored) {
            // Assume 2005 or later
        }
        String viewNameEscaped = database.escapeObjectName(new ObjectReference(schema.getCatalogName(), schema.getSchemaName(), statement.getViewName()), View.class);
        String sql;
        if (sql2005OrLater) {
            sql = "SELECT OBJECT_DEFINITION(OBJECT_ID(N'" + database.escapeStringForDatabase(viewNameEscaped) + "')) AS [ObjectDefinition]";
        } else {
            sql =
                    "SELECT [c].[text] " +
                    "FROM [dbo].[syscomments] AS [c] " +
                    "WHERE [c].[id] = OBJECT_ID(N'" + database.escapeStringForDatabase(viewNameEscaped) + "') " +
                    "ORDER BY [c].[colid]";
        }
        return new Sql[] { new UnparsedSql(sql) };
    }
}
