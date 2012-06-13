package liquibase.sqlgenerator.core;

import liquibase.database.Database;
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
        String sql = "select view_definition from information_schema.views where upper(table_name)='" + statement.getViewName().toUpperCase() + "'";
        String schema = database.correctSchemaName(statement.getSchemaName());
        String catalog = database.correctCatalogName(statement.getCatalogName());
        if (schema != null) {
            sql += " and table_schema='" + schema + "'";
        }

        if (catalog != null) {
            sql += " and table_catalog='" + catalog + "'";
        }

        return new Sql[]{
                new UnparsedSql(sql)
        };
    }
}
