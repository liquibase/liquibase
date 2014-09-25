package liquibase.statementlogic.core;

import liquibase.CatalogAndSchema;
import liquibase.action.Action;
import liquibase.action.core.TablesJdbcMetaDataQueryAction;
import liquibase.database.core.DerbyDatabase;
import liquibase.exception.UnsupportedException;
import liquibase.statement.core.SelectMetaDataStatement;
import liquibase.statementlogic.AbstractStatementLogic;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.structure.core.Table;

public class SelectTablesLogic extends AbstractStatementLogic<SelectMetaDataStatement> {

    @Override
    public boolean supports(SelectMetaDataStatement statement, ExecutionEnvironment env) {
        DatabaseConnection connection = env.getTargetDatabase().getConnection();
        if (connection == null || connection instanceof JdbcConnection || connection instanceof OfflineConnection) {
            return statement.getExample() instanceof Table;
        } else {
            return false;
        }

    }

    @Override
    public ValidationErrors validate(SelectMetaDataStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        Database database = env.getTargetDatabase();
        ValidationErrors errors = super.validate(statement, env, chain);
        Table example = (Table) statement.getExample();
        if (example.getSchema() != null && example.getSchema().getCatalogName() != null && example.getSchema().getName() != null) {
            if (!example.getSchema().getCatalogName().equals(example.getSchema().getName()) && !database.supportsSchemas()) {
                errors.addError("Database "+ database.getShortName()+" does not support separate catalogs and schemas");
            }
        }
        return errors;
    }

    @Override
    public Action[] generateActions(final SelectMetaDataStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();
        if (database.getConnection() == null || database.getConnection() instanceof OfflineConnection) {
            throw new UnexpectedLiquibaseException("Cannot read table metadata for an offline database");
        }
        Table example = (Table) statement.getExample();
        String tableName = null;
        if (example.getName() != null) {
            if (database instanceof PostgresDatabase) {
                tableName = example.getName().toLowerCase();
            } else {
                tableName = example.getName().toUpperCase();
            }
        }
        String catalogName = null;
        String schemaName = null;
        if (example.getSchema() != null) {
            catalogName = example.getSchema().getCatalogName();
            schemaName = example.getSchema().getName();
        }
        CatalogAndSchema catalogAndSchema = new CatalogAndSchema(catalogName, schemaName).customize(database);
        catalogName = ((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema);
        schemaName = ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema);

        if (database instanceof DerbyDatabase) {
            schemaName = catalogName;
            catalogName = null;
        }

        return new Action[] {
                new TablesJdbcMetaDataQueryAction(catalogName, schemaName, tableName)
        };
    }


    @Override
    public boolean generateActionsIsVolatile(ExecutionEnvironment env) {
        return false;
    }

}
