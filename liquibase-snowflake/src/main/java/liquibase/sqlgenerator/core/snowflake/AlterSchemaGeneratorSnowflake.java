package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.AlterSchemaStatement;
import liquibase.structure.core.Table;
import liquibase.ext.SnowflakeNamespaceAttributeStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlterSchemaGeneratorSnowflake extends AbstractSqlGenerator<AlterSchemaStatement> {

    @Override
    public boolean supports(AlterSchemaStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(AlterSchemaStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = new ValidationErrors();
        
        errors.checkRequiredField("schemaName", statement.getSchemaName());
        
        // Check for namespace attributes
        Map<String, String> namespaceAttrs = SnowflakeNamespaceAttributeStorage.getAttributes(statement.getSchemaName());
        boolean hasNamespaceChanges = namespaceAttrs != null && !namespaceAttrs.isEmpty();
        
        // At least one change must be specified (including namespace attributes)
        if (!hasNamespaceChanges &&
            statement.getNewName() == null && 
            statement.getNewDataRetentionTimeInDays() == null && 
            statement.getNewMaxDataExtensionTimeInDays() == null &&
            statement.getNewDefaultDdlCollation() == null &&
            statement.getNewComment() == null &&
            statement.getNewPipeExecutionPaused() == null &&
            (statement.getDropComment() == null || !statement.getDropComment()) &&
            (statement.getEnableManagedAccess() == null || !statement.getEnableManagedAccess()) &&
            (statement.getDisableManagedAccess() == null || !statement.getDisableManagedAccess()) &&
            (statement.getUnsetDataRetentionTimeInDays() == null || !statement.getUnsetDataRetentionTimeInDays()) &&
            (statement.getUnsetMaxDataExtensionTimeInDays() == null || !statement.getUnsetMaxDataExtensionTimeInDays()) &&
            (statement.getUnsetDefaultDdlCollation() == null || !statement.getUnsetDefaultDdlCollation()) &&
            (statement.getUnsetPipeExecutionPaused() == null || !statement.getUnsetPipeExecutionPaused()) &&
            (statement.getUnsetComment() == null || !statement.getUnsetComment())) {
            errors.addError("At least one schema property must be changed");
        }
        
        // Validate namespace attributes if present
        if (hasNamespaceChanges) {
            // Validate data retention constraints
            String newDataRetentionDays = namespaceAttrs.get("newDataRetentionTimeInDays");
            String newMaxDataExtensionDays = namespaceAttrs.get("newMaxDataExtensionTimeInDays");
            
            if (newDataRetentionDays != null && newMaxDataExtensionDays != null) {
                try {
                    int dataRetention = Integer.parseInt(newDataRetentionDays);
                    int maxDataExtension = Integer.parseInt(newMaxDataExtensionDays);
                    
                    if (dataRetention < 0 || dataRetention > 90) {
                        errors.addError("DATA_RETENTION_TIME_IN_DAYS must be between 0 and 90");
                    }
                    if (maxDataExtension < 0 || maxDataExtension > 90) {
                        errors.addError("MAX_DATA_EXTENSION_TIME_IN_DAYS must be between 0 and 90");
                    }
                    if (maxDataExtension < dataRetention) {
                        errors.addError("MAX_DATA_EXTENSION_TIME_IN_DAYS must be >= DATA_RETENTION_TIME_IN_DAYS");
                    }
                } catch (NumberFormatException e) {
                    errors.addError("DATA_RETENTION_TIME_IN_DAYS and MAX_DATA_EXTENSION_TIME_IN_DAYS must be valid integers");
                }
            }
            
            // Validate comment length  
            String newComment = namespaceAttrs.get("newComment");
            if (newComment != null && newComment.length() > 256) {
                errors.addError("Schema comment cannot exceed 256 characters");
            }
            
            // Validate mutual exclusivity for comment operations
            boolean hasSetComment = newComment != null;
            boolean hasUnsetComment = Boolean.parseBoolean(namespaceAttrs.get("unsetComment"));
            boolean hasDropComment = Boolean.parseBoolean(namespaceAttrs.get("dropComment"));
            
            if ((hasSetComment && hasUnsetComment) || (hasSetComment && hasDropComment) || (hasUnsetComment && hasDropComment)) {
                errors.addError("Cannot SET and UNSET comment operations simultaneously");
            }
            
            // Validate mutual exclusivity for managed access operations
            boolean hasEnableManagedAccess = Boolean.parseBoolean(namespaceAttrs.get("enableManagedAccess"));
            boolean hasDisableManagedAccess = Boolean.parseBoolean(namespaceAttrs.get("disableManagedAccess"));
            
            if (hasEnableManagedAccess && hasDisableManagedAccess) {
                errors.addError("Cannot ENABLE and DISABLE managed access simultaneously");
            }
        }
        
        return errors;
    }

    @Override
    public Sql[] generateSql(AlterSchemaStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // Check validation first - prevent generating invalid SQL
        ValidationErrors errors = validate(statement, database, sqlGeneratorChain);
        if (errors.hasErrors()) {
            throw new RuntimeException("Validation failed for AlterSchema: " + errors.toString());
        }
        
        List<Sql> sqlList = new ArrayList<>();
        
        // Process namespace attributes first - they override statement attributes
        Map<String, String> namespaceAttrs = SnowflakeNamespaceAttributeStorage.getAttributes(statement.getSchemaName());
        if (namespaceAttrs != null && !namespaceAttrs.isEmpty()) {
            // Extract namespace attributes
            String newName = namespaceAttrs.get("newName");
            String newDataRetentionTimeInDays = namespaceAttrs.get("newDataRetentionTimeInDays");
            String newMaxDataExtensionTimeInDays = namespaceAttrs.get("newMaxDataExtensionTimeInDays");
            String newDefaultDdlCollation = namespaceAttrs.get("newDefaultDdlCollation");
            String newComment = namespaceAttrs.get("newComment");
            String newPipeExecutionPaused = namespaceAttrs.get("newPipeExecutionPaused");
            boolean dropComment = Boolean.parseBoolean(namespaceAttrs.get("dropComment"));
            boolean unsetComment = Boolean.parseBoolean(namespaceAttrs.get("unsetComment"));
            boolean enableManagedAccess = Boolean.parseBoolean(namespaceAttrs.get("enableManagedAccess"));
            boolean disableManagedAccess = Boolean.parseBoolean(namespaceAttrs.get("disableManagedAccess"));
            boolean unsetDataRetentionTimeInDays = Boolean.parseBoolean(namespaceAttrs.get("unsetDataRetentionTimeInDays"));
            boolean unsetMaxDataExtensionTimeInDays = Boolean.parseBoolean(namespaceAttrs.get("unsetMaxDataExtensionTimeInDays"));
            boolean unsetDefaultDdlCollation = Boolean.parseBoolean(namespaceAttrs.get("unsetDefaultDdlCollation"));
            boolean unsetPipeExecutionPaused = Boolean.parseBoolean(namespaceAttrs.get("unsetPipeExecutionPaused"));
            
            // Clean up stored attributes
            SnowflakeNamespaceAttributeStorage.removeAttributes(statement.getSchemaName());
            
            // Process namespace operations with the extracted values
            processAlterSchemaOperations(sqlList, database, statement, 
                newName, newDataRetentionTimeInDays, newMaxDataExtensionTimeInDays, 
                newDefaultDdlCollation, newComment, newPipeExecutionPaused,
                dropComment, unsetComment, enableManagedAccess, disableManagedAccess,
                unsetDataRetentionTimeInDays, unsetMaxDataExtensionTimeInDays, 
                unsetDefaultDdlCollation, unsetPipeExecutionPaused);
        } else {
            // Use statement attributes
            processAlterSchemaOperations(sqlList, database, statement,
                statement.getNewName(), statement.getNewDataRetentionTimeInDays(), 
                statement.getNewMaxDataExtensionTimeInDays(), statement.getNewDefaultDdlCollation(),
                statement.getNewComment(), statement.getNewPipeExecutionPaused(),
                statement.getDropComment() != null && statement.getDropComment(),
                statement.getUnsetComment() != null && statement.getUnsetComment(),
                statement.getEnableManagedAccess() != null && statement.getEnableManagedAccess(),
                statement.getDisableManagedAccess() != null && statement.getDisableManagedAccess(),
                statement.getUnsetDataRetentionTimeInDays() != null && statement.getUnsetDataRetentionTimeInDays(),
                statement.getUnsetMaxDataExtensionTimeInDays() != null && statement.getUnsetMaxDataExtensionTimeInDays(),
                statement.getUnsetDefaultDdlCollation() != null && statement.getUnsetDefaultDdlCollation(),
                statement.getUnsetPipeExecutionPaused() != null && statement.getUnsetPipeExecutionPaused());
        }
        
        return sqlList.toArray(new Sql[0]);
    }
    
    private void processAlterSchemaOperations(List<Sql> sqlList, Database database, AlterSchemaStatement statement,
            String newName, String newDataRetentionTimeInDays, String newMaxDataExtensionTimeInDays,
            String newDefaultDdlCollation, String newComment, String newPipeExecutionPaused,
            boolean dropComment, boolean unsetComment, boolean enableManagedAccess, boolean disableManagedAccess,
            boolean unsetDataRetentionTimeInDays, boolean unsetMaxDataExtensionTimeInDays,
            boolean unsetDefaultDdlCollation, boolean unsetPipeExecutionPaused) {
        
        // Handle RENAME TO separately as it requires a different syntax
        if (newName != null && !newName.isEmpty()) {
            StringBuilder renameSql = new StringBuilder("ALTER SCHEMA ");
            if (statement.getIfExists() != null && statement.getIfExists()) {
                renameSql.append("IF EXISTS ");
            }
            // For schema operations, only qualify with database if catalogName is explicitly provided
            if (statement.getCatalogName() != null && !statement.getCatalogName().isEmpty()) {
                renameSql.append(database.escapeObjectName(statement.getCatalogName(), null, statement.getSchemaName(), Table.class));
                renameSql.append(" RENAME TO ");
                renameSql.append(database.escapeObjectName(statement.getCatalogName(), null, newName, Table.class));
            } else {
                renameSql.append(database.escapeObjectName(statement.getSchemaName(), Table.class));
                renameSql.append(" RENAME TO ");
                renameSql.append(database.escapeObjectName(newName, Table.class));
            }
            sqlList.add(new UnparsedSql(renameSql.toString()));
        }
        
        // Handle managed access operations
        if (enableManagedAccess) {
            StringBuilder managedSql = new StringBuilder("ALTER SCHEMA ");
            if (statement.getIfExists() != null && statement.getIfExists()) {
                managedSql.append("IF EXISTS ");
            }
            String schemaName = (newName != null && !newName.isEmpty()) ? newName : statement.getSchemaName();
            if (statement.getCatalogName() != null && !statement.getCatalogName().isEmpty()) {
                managedSql.append(database.escapeObjectName(statement.getCatalogName(), null, schemaName, Table.class));
            } else {
                managedSql.append(database.escapeObjectName(schemaName, Table.class));
            }
            managedSql.append(" ENABLE MANAGED ACCESS");
            sqlList.add(new UnparsedSql(managedSql.toString()));
        } else if (disableManagedAccess) {
            StringBuilder managedSql = new StringBuilder("ALTER SCHEMA ");
            if (statement.getIfExists() != null && statement.getIfExists()) {
                managedSql.append("IF EXISTS ");
            }
            String schemaName = (newName != null && !newName.isEmpty()) ? newName : statement.getSchemaName();
            if (statement.getCatalogName() != null && !statement.getCatalogName().isEmpty()) {
                managedSql.append(database.escapeObjectName(statement.getCatalogName(), null, schemaName, Table.class));
            } else {
                managedSql.append(database.escapeObjectName(schemaName, Table.class));
            }
            managedSql.append(" DISABLE MANAGED ACCESS");
            sqlList.add(new UnparsedSql(managedSql.toString()));
        }
        
        // Handle SET operations
        List<String> setOptions = new ArrayList<>();
        
        if (newDataRetentionTimeInDays != null && !newDataRetentionTimeInDays.isEmpty()) {
            setOptions.add("DATA_RETENTION_TIME_IN_DAYS = " + newDataRetentionTimeInDays);
        }
        
        if (newMaxDataExtensionTimeInDays != null && !newMaxDataExtensionTimeInDays.isEmpty()) {
            setOptions.add("MAX_DATA_EXTENSION_TIME_IN_DAYS = " + newMaxDataExtensionTimeInDays);
        }
        
        if (newDefaultDdlCollation != null && !newDefaultDdlCollation.isEmpty()) {
            setOptions.add("DEFAULT_DDL_COLLATION = '" + newDefaultDdlCollation.replace("'", "''") + "'");
        }
        
        if (newPipeExecutionPaused != null && !newPipeExecutionPaused.isEmpty()) {
            setOptions.add("PIPE_EXECUTION_PAUSED = " + newPipeExecutionPaused.toUpperCase());
        }
        
        if (newComment != null && !newComment.isEmpty()) {
            setOptions.add("COMMENT = '" + newComment.replace("'", "''") + "'");
        } else if (dropComment) {
            setOptions.add("COMMENT = ''");
        }
        
        if (!setOptions.isEmpty()) {
            StringBuilder setSql = new StringBuilder("ALTER SCHEMA ");
            if (statement.getIfExists() != null && statement.getIfExists()) {
                setSql.append("IF EXISTS ");
            }
            String schemaName = (newName != null && !newName.isEmpty()) ? newName : statement.getSchemaName();
            if (statement.getCatalogName() != null && !statement.getCatalogName().isEmpty()) {
                setSql.append(database.escapeObjectName(statement.getCatalogName(), null, schemaName, Table.class));
            } else {
                setSql.append(database.escapeObjectName(schemaName, Table.class));
            }
            setSql.append(" SET ");
            setSql.append(String.join(" ", setOptions));
            sqlList.add(new UnparsedSql(setSql.toString()));
        }
        
        // Handle UNSET operations
        List<String> unsetOptions = new ArrayList<>();
        
        if (unsetDataRetentionTimeInDays) {
            unsetOptions.add("DATA_RETENTION_TIME_IN_DAYS");
        }
        
        if (unsetMaxDataExtensionTimeInDays) {
            unsetOptions.add("MAX_DATA_EXTENSION_TIME_IN_DAYS");
        }
        
        if (unsetDefaultDdlCollation) {
            unsetOptions.add("DEFAULT_DDL_COLLATION");
        }
        
        if (unsetPipeExecutionPaused) {
            unsetOptions.add("PIPE_EXECUTION_PAUSED");
        }
        
        if (unsetComment) {
            unsetOptions.add("COMMENT");
        }
        
        if (!unsetOptions.isEmpty()) {
            StringBuilder unsetSql = new StringBuilder("ALTER SCHEMA ");
            if (statement.getIfExists() != null && statement.getIfExists()) {
                unsetSql.append("IF EXISTS ");
            }
            String schemaName = (newName != null && !newName.isEmpty()) ? newName : statement.getSchemaName();
            if (statement.getCatalogName() != null && !statement.getCatalogName().isEmpty()) {
                unsetSql.append(database.escapeObjectName(statement.getCatalogName(), null, schemaName, Table.class));
            } else {
                unsetSql.append(database.escapeObjectName(schemaName, Table.class));
            }
            unsetSql.append(" UNSET ");
            unsetSql.append(String.join(" ", unsetOptions));
            sqlList.add(new UnparsedSql(unsetSql.toString()));
        }
    }
}