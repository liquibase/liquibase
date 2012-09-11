package liquibase.sqlgenerator.core;

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
        Schema schema = database.correctSchema(new Schema(statement.getCatalogName(), statement.getSchemaName()));

        String sql = "select view_definition from information_schema.views where table_name='" + database.correctObjectName(statement.getViewName(), View.class) + "'";

        if (database instanceof MySQLDatabase) {
            String catalogName = database.getAssumedCatalogName(schema.getCatalogName(), schema.getName());
            sql += " and table_schema='" + catalogName + "'";
        } else {

            if (database.supportsSchemas()) {
                String schemaName = database.getAssumedSchemaName(schema.getCatalogName(), schema.getName());
                if (schemaName != null) {
                    sql += " and table_schema='" + schemaName + "'";
                }
            }

            if (database.supportsCatalogs()) {
                String catalogName = database.getAssumedCatalogName(schema.getCatalogName(), schema.getName());
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
