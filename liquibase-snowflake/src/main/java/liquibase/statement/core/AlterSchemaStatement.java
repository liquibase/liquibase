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
}