package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
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
            sql += " and table_schema='" + schema.getCatalogName() + "'";
        } else {

            if (database.supportsSchemas()) {
                String schemaName = schema.getSchemaName();
                if (schemaName != null) {
                    sql += " and table_schema='" + schemaName + "'";
                }
            }

            if (database.supportsCatalogs()) {
                String catalogName = schema.getCatalogName();
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
