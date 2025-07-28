package liquibase.statement.core.snowflake;

import liquibase.statement.AbstractSqlStatement;

public class AlterSchemaStatement extends AbstractSqlStatement {
    
    private String databaseName;
    private String schemaName;
    private String newName;
    private String newComment;
    private Integer newDataRetentionTimeInDays;
    private Integer newMaxDataExtensionTimeInDays;
    private String newDefaultDdlCollation;
    private Boolean enableManagedAccess;
    private Boolean disableManagedAccess;
    private String swapWith;
    private Boolean unsetDataRetentionTimeInDays;

    public AlterSchemaStatement(String databaseName, String schemaName, String newName, 
                               String newComment, Integer newDataRetentionTimeInDays,
                               Integer newMaxDataExtensionTimeInDays, String newDefaultDdlCollation,
                               Boolean enableManagedAccess, Boolean disableManagedAccess,
                               String swapWith, Boolean unsetDataRetentionTimeInDays) {
        this.databaseName = databaseName;
        this.schemaName = schemaName;
        this.newName = newName;
        this.newComment = newComment;
        this.newDataRetentionTimeInDays = newDataRetentionTimeInDays;
        this.newMaxDataExtensionTimeInDays = newMaxDataExtensionTimeInDays;
        this.newDefaultDdlCollation = newDefaultDdlCollation;
        this.enableManagedAccess = enableManagedAccess;
        this.disableManagedAccess = disableManagedAccess;
        this.swapWith = swapWith;
        this.unsetDataRetentionTimeInDays = unsetDataRetentionTimeInDays;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getNewName() {
        return newName;
    }

    public String getNewComment() {
        return newComment;
    }

    public Integer getNewDataRetentionTimeInDays() {
        return newDataRetentionTimeInDays;
    }

    public Integer getNewMaxDataExtensionTimeInDays() {
        return newMaxDataExtensionTimeInDays;
    }

    public String getNewDefaultDdlCollation() {
        return newDefaultDdlCollation;
    }

    public Boolean getEnableManagedAccess() {
        return enableManagedAccess;
    }

    public Boolean getDisableManagedAccess() {
        return disableManagedAccess;
    }

    public String getSwapWith() {
        return swapWith;
    }

    public Boolean getUnsetDataRetentionTimeInDays() {
        return unsetDataRetentionTimeInDays;
    }
}