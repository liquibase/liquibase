package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.CreateDatabaseStatement;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.List;

public class CreateDatabaseGeneratorSnowflake extends AbstractSqlGenerator<CreateDatabaseStatement> {

    @Override
    public boolean supports(CreateDatabaseStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(CreateDatabaseStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = new ValidationErrors();
        
        errors.checkRequiredField("databaseName", statement.getDatabaseName());
        
        // Validate that orReplace and ifNotExists are not both set
        if (Boolean.TRUE.equals(statement.getOrReplace()) && Boolean.TRUE.equals(statement.getIfNotExists())) {
            errors.addError("Cannot use both OR REPLACE and IF NOT EXISTS");
        }
        
        // Validate transient databases must have 0 retention time
        if (Boolean.TRUE.equals(statement.getTransient()) && statement.getDataRetentionTimeInDays() != null) {
            try {
                int days = Integer.parseInt(statement.getDataRetentionTimeInDays());
                if (days > 0) {
                    errors.addError("Transient databases must have DATA_RETENTION_TIME_IN_DAYS = 0");
                }
            } catch (NumberFormatException e) {
                errors.addError("Invalid dataRetentionTimeInDays value: " + statement.getDataRetentionTimeInDays());
            }
        }
        
        return errors;
    }

    @Override
    public Sql[] generateSql(CreateDatabaseStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // Check validation first - prevent generating invalid SQL
        ValidationErrors errors = validate(statement, database, sqlGeneratorChain);
        if (errors.hasErrors()) {
            throw new RuntimeException("Validation failed for CreateDatabase: " + errors.toString());
        }
        
        StringBuilder sql = new StringBuilder("CREATE ");
        
        // OR REPLACE must come after CREATE but before TRANSIENT
        if (statement.getOrReplace() != null && statement.getOrReplace()) {
            sql.append("OR REPLACE ");
        }
        
        // TRANSIENT must come after OR REPLACE (if present)
        if (statement.getTransient() != null && statement.getTransient()) {
            sql.append("TRANSIENT ");
        }
        
        sql.append("DATABASE ");
        
        // IF NOT EXISTS must come after DATABASE
        if (statement.getIfNotExists() != null && statement.getIfNotExists()) {
            sql.append("IF NOT EXISTS ");
        }
        
        sql.append(database.escapeObjectName(statement.getDatabaseName(), Table.class));
        
        // Handle CLONE clause - handle both cloneFrom and fromDatabase (alternative names)
        if (statement.getCloneFrom() != null) {
            sql.append(" CLONE ").append(statement.getCloneFrom());
        } else if (statement.getFromDatabase() != null) {
            sql.append(" CLONE ").append(statement.getFromDatabase());
        }
        
        List<String> options = new ArrayList<>();
        
        if (statement.getDataRetentionTimeInDays() != null) {
            options.add("DATA_RETENTION_TIME_IN_DAYS = " + statement.getDataRetentionTimeInDays());
        }
        
        if (statement.getMaxDataExtensionTimeInDays() != null) {
            options.add("MAX_DATA_EXTENSION_TIME_IN_DAYS = " + statement.getMaxDataExtensionTimeInDays());
        }
        
        if (statement.getDefaultDdlCollation() != null) {
            options.add("DEFAULT_DDL_COLLATION = '" + statement.getDefaultDdlCollation() + "'");
        }
        
        if (statement.getExternalVolume() != null) {
            options.add("EXTERNAL_VOLUME = '" + statement.getExternalVolume() + "'");
        }
        
        if (statement.getCatalog() != null) {
            options.add("CATALOG = '" + statement.getCatalog() + "'");
        }
        
        if (statement.getReplaceInvalidCharacters() != null) {
            options.add("REPLACE_INVALID_CHARACTERS = " + statement.getReplaceInvalidCharacters());
        }
        
        if (statement.getStorageSerializationPolicy() != null) {
            options.add("STORAGE_SERIALIZATION_POLICY = '" + statement.getStorageSerializationPolicy() + "'");
        }
        
        if (statement.getCatalogSync() != null) {
            options.add("CATALOG_SYNC = '" + statement.getCatalogSync() + "'");
        }
        
        if (statement.getCatalogSyncNamespaceMode() != null) {
            options.add("CATALOG_SYNC_NAMESPACE_MODE = '" + statement.getCatalogSyncNamespaceMode() + "'");
        }
        
        if (statement.getCatalogSyncNamespaceFlattenDelimiter() != null) {
            options.add("CATALOG_SYNC_NAMESPACE_FLATTEN_DELIMITER = '" + statement.getCatalogSyncNamespaceFlattenDelimiter() + "'");
        }
        
        if (statement.getComment() != null) {
            options.add("COMMENT = '" + statement.getComment().replace("'", "''") + "'");
        }
        
        // Handle TAG clause separately as it has special syntax
        if (statement.getTag() != null) {
            // For now, treat as simple string - could be enhanced for key-value pairs later
            options.add("TAG ('" + statement.getTag() + "')");
        }
        
        if (!options.isEmpty()) {
            sql.append(" ");
            sql.append(String.join(" ", options));
        }

        return new Sql[]{new UnparsedSql(sql.toString())};
    }
}