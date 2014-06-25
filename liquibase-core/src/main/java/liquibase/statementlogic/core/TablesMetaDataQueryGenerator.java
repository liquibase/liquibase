package liquibase.statementlogic.core;

import liquibase.CatalogAndSchema;
import liquibase.action.Action;
import liquibase.action.core.TablesJdbcMetaDataQueryAction;
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
import liquibase.statement.core.MetaDataQueryStatement;
import liquibase.structure.core.Table;

public class TablesMetaDataQueryGenerator extends AbstractStatementLogic<MetaDataQueryStatement> {

    @Override
    public boolean supports(MetaDataQueryStatement statement, ExecutionEnvironment env) {
        DatabaseConnection connection = env.getTargetDatabase().getConnection();
        if (connection == null || connection instanceof JdbcConnection || connection instanceof OfflineConnection) {
            return statement.getExample() instanceof Table;
        } else {
            return false;
        }

    }

    @Override
    public ValidationErrors validate(MetaDataQueryStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
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
    public Action[] generateActions(final MetaDataQueryStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
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

        return new Action[] {
                new TablesJdbcMetaDataQueryAction(catalogName, schemaName, tableName)
        };
    }


    @Override
    public boolean generateStatementsIsVolatile(ExecutionEnvironment env) {
        return false;
    }

    @Override
    public boolean generateRollbackStatementsIsVolatile(ExecutionEnvironment env) {
        return false;
    }
}
