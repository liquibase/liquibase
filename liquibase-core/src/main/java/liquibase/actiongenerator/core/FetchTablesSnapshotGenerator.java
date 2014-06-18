package liquibase.actiongenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.action.Action;
import liquibase.action.metadata.FetchTablesAction;
import liquibase.actiongenerator.AbstractActionGenerator;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.statement.core.FetchObjectsStatement;
import liquibase.structure.core.Table;

public class FetchTablesSnapshotGenerator extends AbstractActionGenerator<FetchObjectsStatement> {

    @Override
    public boolean supports(FetchObjectsStatement statement, Database database) {
        DatabaseConnection connection = database.getConnection();
        if (connection == null || connection instanceof JdbcConnection || connection instanceof OfflineConnection) {
            return statement.getExample() instanceof Table;
        } else {
            return false;
        }

    }

    @Override
    public ValidationErrors validate(FetchObjectsStatement statement, Database database, ActionGeneratorChain chain) {
        ValidationErrors errors = super.validate(statement, database, chain);
        Table example = (Table) statement.getExample();
        if (example.getSchema() != null && example.getSchema().getCatalogName() != null && example.getSchema().getName() != null) {
            if (!example.getSchema().getCatalogName().equals(example.getSchema().getName()) && !database.supportsSchemas()) {
                errors.addError("Database "+database.getShortName()+" does not support separate catalogs and schemas");
            }
        }
        return errors;
    }

    @Override
    public Action[] generateActions(final FetchObjectsStatement statement, Database database, ActionGeneratorChain chain) {
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
                new FetchTablesAction(catalogName, schemaName, tableName)
        };
    }

}
