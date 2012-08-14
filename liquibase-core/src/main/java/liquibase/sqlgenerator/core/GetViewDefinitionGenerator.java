package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.structure.Schema;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
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

        String sql = "select view_definition from information_schema.views where upper(table_name)='" + statement.getViewName().toUpperCase() + "'";

        if (statement.getSchemaName() != null) {
            sql += " and table_schema='" + schema.getName() + "'";
        }

        if (statement.getCatalogName() != null) {
            sql += " and table_catalog='" + schema.getCatalogName() + "'";
        }

        return new Sql[]{
                new UnparsedSql(sql)
        };
    }
}
