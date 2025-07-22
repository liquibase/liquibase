package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
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

    private String schemaName;
    private String newName;
    private String newDataRetentionTimeInDays;
    private String newMaxDataExtensionTimeInDays;
    private String newDefaultDdlCollation;
    private String newComment;
    private Boolean dropComment;
    private String newPipeExecutionPaused;
    private Boolean enableManagedAccess;
    private Boolean disableManagedAccess;

    @DatabaseChangeProperty(description = "Name of the schema to alter", requiredForDatabase = "snowflake")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
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

    @Override
    public SqlStatement[] generateStatements(Database database) {
        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName(getSchemaName());
        statement.setNewName(getNewName());
        statement.setNewDataRetentionTimeInDays(getNewDataRetentionTimeInDays());
        statement.setNewMaxDataExtensionTimeInDays(getNewMaxDataExtensionTimeInDays());
        statement.setNewDefaultDdlCollation(getNewDefaultDdlCollation());
        statement.setNewComment(getNewComment());
        statement.setDropComment(getDropComment());
        statement.setNewPipeExecutionPaused(getNewPipeExecutionPaused());
        statement.setEnableManagedAccess(getEnableManagedAccess());
        statement.setDisableManagedAccess(getDisableManagedAccess());
        
        return new SqlStatement[]{statement};
    }

    @Override
    public String getConfirmationMessage() {
        return "Schema " + getSchemaName() + " altered";
    }

    @Override
    public boolean supportsRollback(Database database) {
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
            (getDisableManagedAccess() == null || !getDisableManagedAccess())) {
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
        
        return errors;
    }
}