package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class AlterDatabaseStatement extends AbstractSqlStatement {
    
    /**
     * Enum defining the type of ALTER DATABASE operation to perform.
     * Each operation type has different valid properties and SQL generation logic.
     */
    public enum OperationType {
        /** RENAME operation: ALTER DATABASE [IF EXISTS] name RENAME TO new_name */
        RENAME,
        /** SET operation: ALTER DATABASE [IF EXISTS] name SET property = value, ... */
        SET,
        /** UNSET operation: ALTER DATABASE [IF EXISTS] name UNSET property, ... */
        UNSET
    }
    
    private OperationType operationType;
    
    private String databaseName;
    private String newName;
    private Boolean ifExists;
    private String newDataRetentionTimeInDays;
    private String newMaxDataExtensionTimeInDays;
    private String newDefaultDdlCollation;
    private String newComment;
    private Boolean replaceComment;
    private Boolean dropComment;
    // UNSET operations
    private Boolean unsetDataRetentionTimeInDays;
    private Boolean unsetMaxDataExtensionTimeInDays;
    private Boolean unsetDefaultDdlCollation;
    private Boolean unsetComment;

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
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

    public Boolean getReplaceComment() {
        return replaceComment;
    }

    public void setReplaceComment(Boolean replaceComment) {
        this.replaceComment = replaceComment;
    }

    public Boolean getDropComment() {
        return dropComment;
    }

    public void setDropComment(Boolean dropComment) {
        this.dropComment = dropComment;
    }

    public Boolean getIfExists() {
        return ifExists;
    }

    public void setIfExists(Boolean ifExists) {
        this.ifExists = ifExists;
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
        if (databaseName == null || databaseName.trim().isEmpty()) {
            result.addError("Database name is required");
        }
        
        if (operationType == null) {
            // If no operation type specified, try to infer from properties
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
        // Try to infer operation type from set properties (for backward compatibility)
        if (newName != null) {
            return OperationType.RENAME;
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
        }
    }

    private void validateRenameOperation(ValidationResult result) {
        if (newName == null || newName.trim().isEmpty()) {
            result.addError("New database name is required for RENAME operation");
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
                if (days < 0 || days > 90) {
                    result.addError("DATA_RETENTION_TIME_IN_DAYS must be between 0 and 90");
                }
            } catch (NumberFormatException e) {
                result.addError("DATA_RETENTION_TIME_IN_DAYS must be a valid integer");
            }
        }
        
        if (newMaxDataExtensionTimeInDays != null) {
            try {
                int days = Integer.parseInt(newMaxDataExtensionTimeInDays);
                if (days < 0 || days > 90) {
                    result.addError("MAX_DATA_EXTENSION_TIME_IN_DAYS must be between 0 and 90");
                }
            } catch (NumberFormatException e) {
                result.addError("MAX_DATA_EXTENSION_TIME_IN_DAYS must be a valid integer");
            }
        }
    }

    private void validateUnsetOperation(ValidationResult result) {
        if (!hasUnsetProperties()) {
            result.addError("At least one property must be specified for UNSET operation");
        }
    }

    private boolean hasSetProperties() {
        return newDataRetentionTimeInDays != null || newMaxDataExtensionTimeInDays != null || 
               newDefaultDdlCollation != null || newComment != null;
    }

    private boolean hasUnsetProperties() {
        return Boolean.TRUE.equals(unsetDataRetentionTimeInDays) || 
               Boolean.TRUE.equals(unsetMaxDataExtensionTimeInDays) ||
               Boolean.TRUE.equals(unsetDefaultDdlCollation) || 
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