package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.action.Action;
import liquibase.action.UnparsedSql;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.statement.core.GetViewDefinitionStatement;
import liquibase.structure.core.View;

public class GetViewDefinitionGenerator extends AbstractSqlGenerator<GetViewDefinitionStatement> {

    @Override
    public ValidationErrors validate(GetViewDefinitionStatement getViewDefinitionStatement, ExecutionOptions options, ActionGeneratorChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("viewName", getViewDefinitionStatement.getViewName());
        return validationErrors;
    }

    @Override
    public Action[] generateActions(GetViewDefinitionStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        CatalogAndSchema schema = new CatalogAndSchema(statement.getCatalogName(), statement.getSchemaName()).customize(database);

        String sql;
        if (database instanceof MSSQLDatabase)
        	sql = "select VIEW_DEFINITION from INFORMATION_SCHEMA.VIEWS where TABLE_NAME='" + database.correctObjectName(statement.getViewName(), View.class) + "'";
        else
        	sql = "select view_definition from information_schema.views where table_name='" + database.correctObjectName(statement.getViewName(), View.class) + "'";

        if (database instanceof MySQLDatabase) {
            sql += " and table_schema='" + schema.getCatalogName() + "'";
        } else {

            if (database.supportsSchemas()) {
                String schemaName = schema.getSchemaName();
                if (schemaName != null) {
                	if (database instanceof MSSQLDatabase)
                		sql += " and TABLE_SCHEMA='" + schemaName + "'";
                	else
                		sql += " and table_schema='" + schemaName + "'";
                }
            }

            if (database.supportsCatalogs()) {
                String catalogName = schema.getCatalogName();
                if (catalogName != null) {
                	if (database instanceof MSSQLDatabase)
                		sql += " and TABLE_CATALOG='" + catalogName + "'";
                	else
                		sql += " and table_catalog='" + catalogName + "'";
                }
            }
        }

        return new Action[]{
                new UnparsedSql(sql)
        };
    }
}
