package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class AlterSchemaStatement extends AbstractSqlStatement {
    
    /**
     * Enum defining the type of ALTER SCHEMA operation to perform.
     * Each operation type has different valid properties and SQL generation logic.
     */
    public enum OperationType {
        /** RENAME operation: ALTER SCHEMA [IF EXISTS] name RENAME TO new_name */
        RENAME,
        /** SET operation: ALTER SCHEMA [IF EXISTS] name SET property = value, ... */
        SET,
        /** UNSET operation: ALTER SCHEMA [IF EXISTS] name UNSET property, ... */
        UNSET,
        /** ENABLE MANAGED ACCESS operation: ALTER SCHEMA [IF EXISTS] name ENABLE MANAGED ACCESS */
        ENABLE_MANAGED_ACCESS,
        /** DISABLE MANAGED ACCESS operation: ALTER SCHEMA [IF EXISTS] name DISABLE MANAGED ACCESS */
        DISABLE_MANAGED_ACCESS
    }
    
    private OperationType operationType;
    
    private String schemaName;
    private String catalogName;
    private Boolean ifExists;
    private String newName;
    private String newDataRetentionTimeInDays;
    private String newMaxDataExtensionTimeInDays;
    private String newDefaultDdlCollation;
    private String newComment;
    private Boolean dropComment;
    private String newPipeExecutionPaused;
    private Boolean enableManagedAccess;
    private Boolean disableManagedAccess;
    
    // UNSET attributes
    private Boolean unsetDataRetentionTimeInDays;
    private Boolean unsetMaxDataExtensionTimeInDays;
    private Boolean unsetDefaultDdlCollation;
    private Boolean unsetPipeExecutionPaused;
    private Boolean unsetComment;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public void setCatalog(String catalog) {
        this.catalogName = catalog;
    }

    public Boolean getIfExists() {
        return ifExists;
    }

    public void setIfExists(Boolean ifExists) {
        this.ifExists = ifExists;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getNewDataRetentionTimeInDays() {
        return newDataRetentionTimeInDays;
    }

    public void setNewDataRetentionTimeInDays(String newDataRetentionTimeInDays) {
        this.newDataRetentionTimeInDays = newDataRetentionTimeInDays;
    }

    public String getNewMaxDataExtensionTimeInDays() {
        return newMaxDataExtensionTimeInDays;
    }

    public void setNewMaxDataExtensionTimeInDays(String newMaxDataExtensionTimeInDays) {
        this.newMaxDataExtensionTimeInDays = newMaxDataExtensionTimeInDays;
    }

    public String getNewDefaultDdlCollation() {
        return newDefaultDdlCollation;
    }

    public void setNewDefaultDdlCollation(String newDefaultDdlCollation) {
        this.newDefaultDdlCollation = newDefaultDdlCollation;
    }

    public String getNewComment() {
        return newComment;
    }

    public void setNewComment(String newComment) {
        this.newComment = newComment;
    }

    public Boolean getDropComment() {
        return dropComment;
    }

    public void setDropComment(Boolean dropComment) {
        this.dropComment = dropComment;
    }

    public String getNewPipeExecutionPaused() {
        return newPipeExecutionPaused;
    }

    public void setNewPipeExecutionPaused(String newPipeExecutionPaused) {
        this.newPipeExecutionPaused = newPipeExecutionPaused;
    }

    public Boolean getEnableManagedAccess() {
        return enableManagedAccess;
    }

    public void setEnableManagedAccess(Boolean enableManagedAccess) {
        this.enableManagedAccess = enableManagedAccess;
    }

    public Boolean getDisableManagedAccess() {
        return disableManagedAccess;
    }

    public void setDisableManagedAccess(Boolean disableManagedAccess) {
        this.disableManagedAccess = disableManagedAccess;
    }

    public Boolean getUnsetDataRetentionTimeInDays() {
        return unsetDataRetentionTimeInDays;
    }

    public void setUnsetDataRetentionTimeInDays(Boolean unsetDataRetentionTimeInDays) {
        this.unsetDataRetentionTimeInDays = unsetDataRetentionTimeInDays;
    }

    public Boolean getUnsetMaxDataExtensionTimeInDays() {
        return unsetMaxDataExtensionTimeInDays;
    }

    public void setUnsetMaxDataExtensionTimeInDays(Boolean unsetMaxDataExtensionTimeInDays) {
        this.unsetMaxDataExtensionTimeInDays = unsetMaxDataExtensionTimeInDays;
    }

    public Boolean getUnsetDefaultDdlCollation() {
        return unsetDefaultDdlCollation;
    }

    public void setUnsetDefaultDdlCollation(Boolean unsetDefaultDdlCollation) {
        this.unsetDefaultDdlCollation = unsetDefaultDdlCollation;
    }

    public Boolean getUnsetPipeExecutionPaused() {
        return unsetPipeExecutionPaused;
    }

    public void setUnsetPipeExecutionPaused(Boolean unsetPipeExecutionPaused) {
        this.unsetPipeExecutionPaused = unsetPipeExecutionPaused;
    }

    public Boolean getUnsetComment() {
        return unsetComment;
    }

    public void setUnsetComment(Boolean unsetComment) {
        this.unsetComment = unsetComment;
    }

    // Enhanced API methods for sophisticated operation-type-driven architecture

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }
    
    /**
     * Enhanced validation method that ensures the statement configuration is valid
     * based on the specified operation type and Snowflake constraints.
     */
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        // Basic validation
        if (schemaName == null || schemaName.trim().isEmpty()) {
            result.addError("Schema name is required");
        } else if (schemaName.length() > 255 || !schemaName.matches("^[A-Za-z_][A-Za-z0-9_$]*$")) {
            result.addError("Invalid schema name format. Must start with letter/underscore, contain only letters/numbers/underscores/dollar signs, max 255 characters: " + schemaName);
        }
        
        if (operationType == null) {
            // Try to infer operation type from properties
            operationType = inferOperationType();
            if (operationType == null) {
                result.addError("Operation type must be specified or inferable from properties");
            }
        }
        
        if (operationType != null) {
            validateOperationType(result);
        }
        
        return result;
    }

    private OperationType inferOperationType() {
        // Try to infer operation type from set properties
        if (newName != null) {
            return OperationType.RENAME;
        }
        
        if (Boolean.TRUE.equals(enableManagedAccess)) {
            return OperationType.ENABLE_MANAGED_ACCESS;
        }
        
        if (Boolean.TRUE.equals(disableManagedAccess)) {
            return OperationType.DISABLE_MANAGED_ACCESS;
        }
        
        // Check for conflicts between different operation types
        boolean hasSetProperties = hasSetProperties();
        boolean hasUnsetProperties = hasUnsetProperties();
        
        // If multiple operation types are specified, cannot infer - this is an error
        int operationTypeCount = (hasSetProperties ? 1 : 0) + (hasUnsetProperties ? 1 : 0);
        if (operationTypeCount > 1) {
            return null; // Cannot infer - multiple operation types specified
        }
        
        if (hasUnsetProperties) {
            return OperationType.UNSET;
        }
        if (hasSetProperties) {
            return OperationType.SET;
        }
        
        return null;
    }

    private void validateOperationType(ValidationResult result) {
        switch (operationType) {
            case RENAME:
                validateRenameOperation(result);
                break;
            case SET:
                validateSetOperation(result);
                break;
            case UNSET:
                validateUnsetOperation(result);
                break;
            case ENABLE_MANAGED_ACCESS:
                validateEnableManagedAccessOperation(result);
                break;
            case DISABLE_MANAGED_ACCESS:
                validateDisableManagedAccessOperation(result);
                break;
        }
    }

    private void validateRenameOperation(ValidationResult result) {
        if (newName == null || newName.trim().isEmpty()) {
            result.addError("New schema name is required for RENAME operation");
        } else if (newName.length() > 255 || !newName.matches("^[A-Za-z_][A-Za-z0-9_$]*$")) {
            result.addError("Invalid new schema name format. Must start with letter/underscore, contain only letters/numbers/underscores/dollar signs, max 255 characters: " + newName);
        }
        
        // RENAME operations should not have other properties set
        if (hasSetProperties()) {
            result.addWarning("SET properties are ignored in RENAME operations");
        }
        if (hasUnsetProperties()) {
            result.addWarning("UNSET properties are ignored in RENAME operations");
        }
    }

    private void validateSetOperation(ValidationResult result) {
        if (!hasSetProperties()) {
            result.addError("At least one property must be specified for SET operation");
        }
        
        // Validate data retention constraints
        if (newDataRetentionTimeInDays != null) {
            try {
                int days = Integer.parseInt(newDataRetentionTimeInDays);
                if (days < 0) {
                    result.addError("DATA_RETENTION_TIME_IN_DAYS cannot be negative, got: " + days);
                } else if (days > 90) {
                    result.addWarning("DATA_RETENTION_TIME_IN_DAYS > 90 days requires Snowflake Enterprise Edition, got: " + days);
                    if (days > 365) {
                        result.addError("DATA_RETENTION_TIME_IN_DAYS cannot exceed 365 days, got: " + days);
                    }
                }
            } catch (NumberFormatException e) {
                result.addError("DATA_RETENTION_TIME_IN_DAYS must be a valid integer, got: " + newDataRetentionTimeInDays);
            }
        }
        
        if (newMaxDataExtensionTimeInDays != null) {
            try {
                int days = Integer.parseInt(newMaxDataExtensionTimeInDays);
                if (days < 0) {
                    result.addError("MAX_DATA_EXTENSION_TIME_IN_DAYS cannot be negative, got: " + days);
                } else if (days > 14) {
                    result.addError("MAX_DATA_EXTENSION_TIME_IN_DAYS cannot exceed 14 days, got: " + days);
                }
            } catch (NumberFormatException e) {
                result.addError("MAX_DATA_EXTENSION_TIME_IN_DAYS must be a valid integer, got: " + newMaxDataExtensionTimeInDays);
            }
        }
        
        // Validate pipe execution paused enumeration
        if (newPipeExecutionPaused != null) {
            String[] validStates = {"TRUE", "FALSE"};
            boolean validState = false;
            for (String validSt : validStates) {
                if (validSt.equalsIgnoreCase(newPipeExecutionPaused)) {
                    validState = true;
                    break;
                }
            }
            if (!validState) {
                result.addError("Invalid PIPE_EXECUTION_PAUSED value '" + newPipeExecutionPaused + "'. Valid values: TRUE, FALSE");
            }
        }
    }

    private void validateUnsetOperation(ValidationResult result) {
        if (!hasUnsetProperties()) {
            result.addError("At least one property must be specified for UNSET operation");
        }
    }

    private void validateEnableManagedAccessOperation(ValidationResult result) {
        result.addWarning("Managed access schemas require Snowflake Enterprise Edition");
        if (hasSetProperties() || hasUnsetProperties()) {
            result.addWarning("Other properties are ignored in ENABLE MANAGED ACCESS operations");
        }
    }

    private void validateDisableManagedAccessOperation(ValidationResult result) {
        if (hasSetProperties() || hasUnsetProperties()) {
            result.addWarning("Other properties are ignored in DISABLE MANAGED ACCESS operations");
        }
    }

    private boolean hasSetProperties() {
        return newDataRetentionTimeInDays != null || newMaxDataExtensionTimeInDays != null || 
               newDefaultDdlCollation != null || newComment != null || 
               Boolean.TRUE.equals(dropComment) || newPipeExecutionPaused != null;
    }

    private boolean hasUnsetProperties() {
        return Boolean.TRUE.equals(unsetDataRetentionTimeInDays) || 
               Boolean.TRUE.equals(unsetMaxDataExtensionTimeInDays) ||
               Boolean.TRUE.equals(unsetDefaultDdlCollation) || 
               Boolean.TRUE.equals(unsetPipeExecutionPaused) ||
               Boolean.TRUE.equals(unsetComment);
    }

    /**
     * Simple validation result container
     */
    public static class ValidationResult {
        private final java.util.List<String> errors = new java.util.ArrayList<>();
        private final java.util.List<String> warnings = new java.util.ArrayList<>();
        
        public boolean hasErrors() { return !errors.isEmpty(); }
        public boolean hasWarnings() { return !warnings.isEmpty(); }
        public java.util.List<String> getErrors() { return errors; }
        public java.util.List<String> getWarnings() { return warnings; }
        
        public void addError(String error) { errors.add(error); }
        public void addWarning(String warning) { warnings.add(warning); }
    }
}