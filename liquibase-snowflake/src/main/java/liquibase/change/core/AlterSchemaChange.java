package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AlterSchemaStatement;

/**
 * Alters a schema.
 */
@DatabaseChange(
    name = "alterSchema",
    description = "Alters a schema",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "schema",
    since = "4.33"
)
public class AlterSchemaChange extends AbstractChange {

    private String operationType; // Enhanced: explicit operation type
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

    @DatabaseChangeProperty(description = "Name of the schema to alter", requiredForDatabase = "snowflake")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(description = "Catalog (database) name")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }


    @DatabaseChangeProperty(description = "Only alter if schema exists")
    public Boolean getIfExists() {
        return ifExists;
    }

    public void setIfExists(Boolean ifExists) {
        this.ifExists = ifExists;
    }

    @DatabaseChangeProperty(description = "New name for the schema")
    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    @DatabaseChangeProperty(description = "New data retention time in days")
    public String getNewDataRetentionTimeInDays() {
        return newDataRetentionTimeInDays;
    }

    public void setNewDataRetentionTimeInDays(String newDataRetentionTimeInDays) {
        this.newDataRetentionTimeInDays = newDataRetentionTimeInDays;
    }

    @DatabaseChangeProperty(description = "New maximum data extension time in days")
    public String getNewMaxDataExtensionTimeInDays() {
        return newMaxDataExtensionTimeInDays;
    }

    public void setNewMaxDataExtensionTimeInDays(String newMaxDataExtensionTimeInDays) {
        this.newMaxDataExtensionTimeInDays = newMaxDataExtensionTimeInDays;
    }

    @DatabaseChangeProperty(description = "New default DDL collation")
    public String getNewDefaultDdlCollation() {
        return newDefaultDdlCollation;
    }

    public void setNewDefaultDdlCollation(String newDefaultDdlCollation) {
        this.newDefaultDdlCollation = newDefaultDdlCollation;
    }

    @DatabaseChangeProperty(description = "New comment for the schema")
    public String getNewComment() {
        return newComment;
    }

    public void setNewComment(String newComment) {
        this.newComment = newComment;
    }

    @DatabaseChangeProperty(description = "Whether to drop the existing comment")
    public Boolean getDropComment() {
        return dropComment;
    }

    public void setDropComment(Boolean dropComment) {
        this.dropComment = dropComment;
    }

    @DatabaseChangeProperty(description = "New pipe execution paused setting")
    public String getNewPipeExecutionPaused() {
        return newPipeExecutionPaused;
    }

    public void setNewPipeExecutionPaused(String newPipeExecutionPaused) {
        this.newPipeExecutionPaused = newPipeExecutionPaused;
    }

    @DatabaseChangeProperty(description = "Enable managed access for the schema")
    public Boolean getEnableManagedAccess() {
        return enableManagedAccess;
    }

    public void setEnableManagedAccess(Boolean enableManagedAccess) {
        this.enableManagedAccess = enableManagedAccess;
    }

    @DatabaseChangeProperty(description = "Disable managed access for the schema")
    public Boolean getDisableManagedAccess() {
        return disableManagedAccess;
    }

    public void setDisableManagedAccess(Boolean disableManagedAccess) {
        this.disableManagedAccess = disableManagedAccess;
    }

    @DatabaseChangeProperty(description = "Remove data retention time setting")
    public Boolean getUnsetDataRetentionTimeInDays() {
        return unsetDataRetentionTimeInDays;
    }

    public void setUnsetDataRetentionTimeInDays(Boolean unsetDataRetentionTimeInDays) {
        this.unsetDataRetentionTimeInDays = unsetDataRetentionTimeInDays;
    }

    @DatabaseChangeProperty(description = "Remove max data extension time setting")
    public Boolean getUnsetMaxDataExtensionTimeInDays() {
        return unsetMaxDataExtensionTimeInDays;
    }

    public void setUnsetMaxDataExtensionTimeInDays(Boolean unsetMaxDataExtensionTimeInDays) {
        this.unsetMaxDataExtensionTimeInDays = unsetMaxDataExtensionTimeInDays;
    }

    @DatabaseChangeProperty(description = "Remove default DDL collation setting")
    public Boolean getUnsetDefaultDdlCollation() {
        return unsetDefaultDdlCollation;
    }

    public void setUnsetDefaultDdlCollation(Boolean unsetDefaultDdlCollation) {
        this.unsetDefaultDdlCollation = unsetDefaultDdlCollation;
    }

    @DatabaseChangeProperty(description = "Remove pipe execution paused setting")
    public Boolean getUnsetPipeExecutionPaused() {
        return unsetPipeExecutionPaused;
    }

    public void setUnsetPipeExecutionPaused(Boolean unsetPipeExecutionPaused) {
        this.unsetPipeExecutionPaused = unsetPipeExecutionPaused;
    }

    @DatabaseChangeProperty(description = "Remove comment setting")
    public Boolean getUnsetComment() {
        return unsetComment;
    }

    public void setUnsetComment(Boolean unsetComment) {
        this.unsetComment = unsetComment;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName(getSchemaName());
        // Use catalogName for database qualification
        if (getCatalogName() != null) {
            statement.setCatalog(getCatalogName());
        }
        statement.setIfExists(getIfExists());
        statement.setNewName(getNewName());
        statement.setNewDataRetentionTimeInDays(getNewDataRetentionTimeInDays());
        statement.setNewMaxDataExtensionTimeInDays(getNewMaxDataExtensionTimeInDays());
        statement.setNewDefaultDdlCollation(getNewDefaultDdlCollation());
        statement.setNewComment(getNewComment());
        statement.setDropComment(getDropComment());
        statement.setNewPipeExecutionPaused(getNewPipeExecutionPaused());
        statement.setEnableManagedAccess(getEnableManagedAccess());
        statement.setDisableManagedAccess(getDisableManagedAccess());
        statement.setUnsetDataRetentionTimeInDays(getUnsetDataRetentionTimeInDays());
        statement.setUnsetMaxDataExtensionTimeInDays(getUnsetMaxDataExtensionTimeInDays());
        statement.setUnsetDefaultDdlCollation(getUnsetDefaultDdlCollation());
        statement.setUnsetPipeExecutionPaused(getUnsetPipeExecutionPaused());
        statement.setUnsetComment(getUnsetComment());
        
        // Enhanced Phase 2 API: Set explicit operation type if provided
        if (getOperationType() != null && !getOperationType().trim().isEmpty()) {
            try {
                AlterSchemaStatement.OperationType opType = 
                    AlterSchemaStatement.OperationType.valueOf(getOperationType().toUpperCase());
                statement.setOperationType(opType);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid operation type: " + getOperationType() + 
                    ". Valid types are: RENAME, SET, UNSET, ENABLE_MANAGED_ACCESS, DISABLE_MANAGED_ACCESS");
            }
        }

        return new SqlStatement[]{statement};
    }

    @Override
    public String getConfirmationMessage() {
        return "Schema " + getSchemaName() + " altered";
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override    public boolean supportsRollback(Database database) {
        return false;
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors errors = super.validate(database);
        
        if (getSchemaName() == null || getSchemaName().trim().isEmpty()) {
            errors.addError("schemaName is required");
        }
        
        // At least one change must be specified
        if (getNewName() == null && 
            getNewDataRetentionTimeInDays() == null && 
            getNewMaxDataExtensionTimeInDays() == null &&
            getNewDefaultDdlCollation() == null &&
            getNewComment() == null &&
            getNewPipeExecutionPaused() == null &&
            (getDropComment() == null || !getDropComment()) &&
            (getEnableManagedAccess() == null || !getEnableManagedAccess()) &&
            (getDisableManagedAccess() == null || !getDisableManagedAccess()) &&
            (getUnsetDataRetentionTimeInDays() == null || !getUnsetDataRetentionTimeInDays()) &&
            (getUnsetMaxDataExtensionTimeInDays() == null || !getUnsetMaxDataExtensionTimeInDays()) &&
            (getUnsetDefaultDdlCollation() == null || !getUnsetDefaultDdlCollation()) &&
            (getUnsetPipeExecutionPaused() == null || !getUnsetPipeExecutionPaused()) &&
            (getUnsetComment() == null || !getUnsetComment())) {
            errors.addError("At least one schema property must be changed");
        }
        
        // Cannot specify both newComment and dropComment
        if (getNewComment() != null && getDropComment() != null && getDropComment()) {
            errors.addError("Cannot specify both newComment and dropComment");
        }
        
        // Cannot specify both enable and disable managed access
        if (getEnableManagedAccess() != null && getEnableManagedAccess() &&
            getDisableManagedAccess() != null && getDisableManagedAccess()) {
            errors.addError("Cannot specify both enableManagedAccess and disableManagedAccess");
        }
        
        // Cannot SET and UNSET the same property
        if (getNewDataRetentionTimeInDays() != null && getUnsetDataRetentionTimeInDays() != null && getUnsetDataRetentionTimeInDays()) {
            errors.addError("Cannot specify both newDataRetentionTimeInDays and unsetDataRetentionTimeInDays");
        }
        
        if (getNewMaxDataExtensionTimeInDays() != null && getUnsetMaxDataExtensionTimeInDays() != null && getUnsetMaxDataExtensionTimeInDays()) {
            errors.addError("Cannot specify both newMaxDataExtensionTimeInDays and unsetMaxDataExtensionTimeInDays");
        }
        
        if (getNewDefaultDdlCollation() != null && getUnsetDefaultDdlCollation() != null && getUnsetDefaultDdlCollation()) {
            errors.addError("Cannot specify both newDefaultDdlCollation and unsetDefaultDdlCollation");
        }
        
        if (getNewPipeExecutionPaused() != null && getUnsetPipeExecutionPaused() != null && getUnsetPipeExecutionPaused()) {
            errors.addError("Cannot specify both newPipeExecutionPaused and unsetPipeExecutionPaused");
        }
        
        if ((getNewComment() != null || (getDropComment() != null && getDropComment())) && 
            getUnsetComment() != null && getUnsetComment()) {
            errors.addError("Cannot specify comment operations (set/drop) with unsetComment");
        }
        
        return errors;
    }

    // Enhanced Phase 2 API: Explicit operation type support

    @DatabaseChangeProperty(description = "Type of ALTER SCHEMA operation (RENAME, SET, UNSET, ENABLE_MANAGED_ACCESS, DISABLE_MANAGED_ACCESS)")
    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    // Additional getter/setter methods for compatibility with ChangedSchemaChangeGenerator

    @DatabaseChangeProperty(description = "New comment for the schema")
    public String getComment() {
        return newComment;
    }

    public void setComment(String comment) {
        this.newComment = comment;
    }

    @DatabaseChangeProperty(description = "New data retention time in days")
    public String getDataRetentionTimeInDays() {
        return newDataRetentionTimeInDays;
    }

    public void setDataRetentionTimeInDays(String dataRetentionTimeInDays) {
        this.newDataRetentionTimeInDays = dataRetentionTimeInDays;
    }

    @DatabaseChangeProperty(description = "Enable/disable managed access for the schema")
    public Boolean getManagedAccess() {
        if (enableManagedAccess != null && enableManagedAccess) {
            return true;
        }
        if (disableManagedAccess != null && disableManagedAccess) {
            return false;
        }
        return null;
    }

    public void setManagedAccess(Boolean managedAccess) {
        if (managedAccess != null) {
            if (managedAccess) {
                this.enableManagedAccess = true;
                this.disableManagedAccess = false;
            } else {
                this.enableManagedAccess = false;
                this.disableManagedAccess = true;
            }
        }
    }
    
    @Override
    public String getSerializedObjectNamespace() {
        return "http://www.liquibase.org/xml/ns/snowflake";
    }
}