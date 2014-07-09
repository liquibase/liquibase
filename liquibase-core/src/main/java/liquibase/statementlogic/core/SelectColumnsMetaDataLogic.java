package liquibase.statementlogic.core;

import liquibase.CatalogAndSchema;
import liquibase.action.Action;
import liquibase.action.core.ColumnsJdbcMetaDataQueryAction;
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
import liquibase.structure.core.Column;

public class SelectColumnsMetaDataLogic extends AbstractStatementLogic<SelectMetaDataStatement> {

    @Override
    public boolean supports(SelectMetaDataStatement statement, ExecutionEnvironment env) {
        DatabaseConnection connection = env.getTargetDatabase().getConnection();
        if (connection == null || connection instanceof JdbcConnection || connection instanceof OfflineConnection) {
            return statement.getExample() instanceof Column;
        } else {
            return false;
        }

    }

    @Override
    public ValidationErrors validate(SelectMetaDataStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        Database database = env.getTargetDatabase();
        ValidationErrors errors = super.validate(statement, env, chain);
        Column example = (Column) statement.getExample();
        if (example.getSchema() != null && example.getSchema().getCatalogName() != null && example.getSchema().getName() != null) {
            if (!example.getSchema().getCatalogName().equals(example.getSchema().getName()) && !database.supportsSchemas()) {
                errors.addError("Database "+ database.getShortName()+" does not support separate catalogs and schemas");
            }
        }
        return errors;
    }

    @Override
    public boolean generateActionsIsVolatile(ExecutionEnvironment env) {
        return false;
    }

    @Override
    public Action[] generateActions(final SelectMetaDataStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();

        if (database.getConnection() == null || database.getConnection() instanceof OfflineConnection) {
            throw new UnexpectedLiquibaseException("Cannot read table metadata for an offline database");
        }
        Column example = (Column) statement.getExample();
        String tableName = null;
        if (example.getRelation() != null) {
            if (database instanceof PostgresDatabase) {
                tableName = example.getRelation().getName().toLowerCase();
            } else {
                tableName = example.getRelation().getName().toUpperCase();
            }
        }

        String columnName = null;
        if (example.getName() != null) {
            if (database instanceof PostgresDatabase) {
                columnName = example.getName().toLowerCase();
            } else {
                columnName = example.getName().toUpperCase();
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
                new ColumnsJdbcMetaDataQueryAction(catalogName, schemaName, tableName, columnName)
        };
    }
}
