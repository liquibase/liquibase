package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.structure.core.Schema;
import liquibase.structure.core.View;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.GetViewDefinitionStatement;

public class GetViewDefinitionGenerator extends AbstractSqlGenerator<GetViewDefinitionStatement> {

    public ValidationErrors validate(GetViewDefinitionStatement getViewDefinitionStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("viewName", getViewDefinitionStatement.getViewName());
        return validationErrors;
    }

    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        CatalogAndSchema schema = database.correctSchema(new CatalogAndSchema(statement.getCatalogName(), statement.getSchemaName()));

        String sql = "select view_definition from information_schema.views where table_name='" + database.correctObjectName(statement.getViewName(), View.class) + "'";

        if (database instanceof MySQLDatabase) {
            String catalogName = database.getAssumedCatalogName(schema.getCatalogName(), schema.getSchemaName());
            sql += " and table_schema='" + catalogName + "'";
        } else {

            if (database.supportsSchemas()) {
                String schemaName = database.getAssumedSchemaName(schema.getCatalogName(), schema.getSchemaName());
                if (schemaName != null) {
                    sql += " and table_schema='" + schemaName + "'";
                }
            }

            if (database.supportsCatalogs()) {
                String catalogName = database.getAssumedCatalogName(schema.getCatalogName(), schema.getSchemaName());
                if (catalogName != null) {
                    sql += " and table_catalog='" + catalogName + "'";
                }
            }
        }

        return new Sql[]{
                new UnparsedSql(sql)
        };
    }
}
