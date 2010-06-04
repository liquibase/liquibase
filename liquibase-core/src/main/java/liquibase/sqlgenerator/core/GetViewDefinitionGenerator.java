package liquibase.sqlgenerator.core;

import liquibase.database.Database;
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
        try {
            String sql = "select view_definition from information_schema.views where upper(table_name)='" + statement.getViewName().toUpperCase() + "'";
            if (database.convertRequestedSchemaToCatalog(statement.getSchemaName()) != null) {
                sql += " and table_schema='" + database.convertRequestedSchemaToSchema(statement.getSchemaName()) + "'";
            } else if (database.convertRequestedSchemaToCatalog(statement.getSchemaName()) != null) {
                sql += " and table_catalog='" + database.convertRequestedSchemaToCatalog(statement.getSchemaName()) + "'";
            }

            return new Sql[] {
                    new UnparsedSql(sql)
            };
        } catch (DatabaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }
}
