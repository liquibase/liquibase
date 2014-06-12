package liquibase.actiongenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.action.Action;
import liquibase.action.ExecuteAction;
import liquibase.action.MetaDataAction;
import liquibase.actiongenerator.AbstractActionGenerator;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.database.core.DB2Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecuteResult;
import liquibase.executor.ExecutionOptions;
import liquibase.executor.QueryResult;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddForeignKeyConstraintStatement;
import liquibase.statement.core.DescribeTablesStatement;
import liquibase.structure.DatabaseObject;
import liquibase.util.JdbcUtils;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collection;

public class DescribeTablesGenerator extends AbstractActionGenerator<DescribeTablesStatement> {

    @Override
    public boolean supports(DescribeTablesStatement statement, Database database) {
        DatabaseConnection connection = database.getConnection();
        return connection == null || connection instanceof JdbcConnection || connection instanceof OfflineConnection;
    }

    @Override
    public ValidationErrors validate(DescribeTablesStatement statement, Database database, ActionGeneratorChain chain) {
        ValidationErrors errors = super.validate(statement, database, chain);
        if (statement.getCatalogName() != null && statement.getSchemaName() != null) {
            if (!statement.getCatalogName().equals(statement.getSchemaName()) && !database.supportsSchemas()) {
                errors.addError("Database "+database.getShortName()+" does not support separate catalogs and schemas");
            }
        }
        return errors;
    }

    @Override
    public Action[] generateActions(final DescribeTablesStatement statement, Database database, ActionGeneratorChain chain) {
        if (database.getConnection() == null || database.getConnection() instanceof OfflineConnection) {
            throw new UnexpectedLiquibaseException("Cannot read table metadata for an offline database");
        }
        String tableName = null;
        if (database instanceof PostgresDatabase) {
            tableName = statement.getTableName().toLowerCase();
        } else {
            tableName = statement.getTableName().toUpperCase();
        }
        CatalogAndSchema catalogAndSchema = new CatalogAndSchema(statement.getCatalogName(), statement.getSchemaName()).customize(database);
        String catalogName = ((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema);
        String schemaName = ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema);

        return new Action[] {
                new MetaDataAction("getTables", catalogName, schemaName, tableName, new String[] { "TABLE" })
        };
    }
}
