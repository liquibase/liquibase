package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.GetViewDefinitionStatement;
import liquibase.structure.core.View;

/**
 * Snowflake-specific view definition generator.
 * Uses Snowflake-specific query to read full view definition statement from a database.
 */
public class GetViewDefinitionGeneratorSnowflake extends GetViewDefinitionGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        CatalogAndSchema schema = new CatalogAndSchema(statement.getCatalogName(), statement.getSchemaName()).customize(database);
        // We can use non quoted schema/catalog/view names here.
        // SELECT GET_DDL('VIEW', 'TEST.BAD$SCHEMA_NAME.BAD$%^VIEW_NAME', TRUE) - works fine.
        // "TRUE" means that the returned result will be in the full representation
        String fullViewName = database.escapeObjectName(schema.getCatalogName(), schema.getSchemaName(), statement.getViewName(), View.class);
        return new Sql[] {
            new UnparsedSql( "SELECT GET_DDL('VIEW', '" + fullViewName + "', TRUE)"
            )
        };
    }
}
