package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AlterDatabaseStatement;

/**
 * Alters a database.
 */
@DatabaseChange(
    name = "alterDatabase",
    description = "Alters a database",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "database",
    since = "4.33"
)
public class AlterDatabaseChange extends AbstractChange {

    private String databaseName;
    private String newName;
    private String newDataRetentionTimeInDays;
    private String newMaxDataExtensionTimeInDays;
    private String newDefaultDdlCollation;
    private String newComment;
    private Boolean replaceComment;
    private Boolean dropComment;

    @DatabaseChangeProperty(description = "Name of the database to alter", requiredForDatabase = "snowflake")
    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @DatabaseChangeProperty(description = "New name for the database")
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

    @DatabaseChangeProperty(description = "New comment for the database")
    public String getNewComment() {
        return newComment;
    }

    public void setNewComment(String newComment) {
        this.newComment = newComment;
    }

    @DatabaseChangeProperty(description = "Whether to replace the existing comment")
    public Boolean getReplaceComment() {
        return replaceComment;
    }

    public void setReplaceComment(Boolean replaceComment) {
        this.replaceComment = replaceComment;
    }

    @DatabaseChangeProperty(description = "Whether to drop the existing comment")
    public Boolean getDropComment() {
        return dropComment;
    }

    public void setDropComment(Boolean dropComment) {
        this.dropComment = dropComment;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        AlterDatabaseStatement statement = new AlterDatabaseStatement();
        statement.setDatabaseName(getDatabaseName());
        statement.setNewName(getNewName());
        statement.setNewDataRetentionTimeInDays(getNewDataRetentionTimeInDays());
        statement.setNewMaxDataExtensionTimeInDays(getNewMaxDataExtensionTimeInDays());
        statement.setNewDefaultDdlCollation(getNewDefaultDdlCollation());
        statement.setNewComment(getNewComment());
        statement.setReplaceComment(getReplaceComment());
        statement.setDropComment(getDropComment());
        
        return new SqlStatement[]{statement};
    }

    @Override
    public String getConfirmationMessage() {
        return "Database " + getDatabaseName() + " altered";
    }

    @Override
    public boolean supportsRollback(Database database) {
        return false;
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors errors = super.validate(database);
        
        if (getDatabaseName() == null || getDatabaseName().trim().isEmpty()) {
            errors.addError("databaseName is required");
        }
        
        // At least one change must be specified
        if (getNewName() == null && 
            getNewDataRetentionTimeInDays() == null && 
            getNewMaxDataExtensionTimeInDays() == null &&
            getNewDefaultDdlCollation() == null &&
            getNewComment() == null &&
            (getDropComment() == null || !getDropComment())) {
            errors.addError("At least one database property must be changed");
        }
        
        // Cannot specify both newComment and dropComment
        if (getNewComment() != null && getDropComment() != null && getDropComment()) {
            errors.addError("Cannot specify both newComment and dropComment");
        }
        
        return errors;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return "http://www.liquibase.org/xml/ns/snowflake";
    }
}