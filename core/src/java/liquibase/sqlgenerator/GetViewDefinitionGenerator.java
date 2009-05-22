package liquibase.sqlgenerator;

import liquibase.statement.GetViewDefinitionStatement;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.exception.JDBCException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;

public class GetViewDefinitionGenerator implements SqlGenerator<GetViewDefinitionStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(GetViewDefinitionStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(GetViewDefinitionStatement getViewDefinitionStatement, Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("viewName", getViewDefinitionStatement.getViewName());
        return validationErrors;
    }

    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database) {
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
        } catch (JDBCException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }
}
