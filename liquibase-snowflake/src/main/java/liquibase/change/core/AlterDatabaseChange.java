package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;import liquibase.exception.ValidationErrors;
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
    public String getNewDatabaseName() {
        return newName;
    }

    public void setNewDatabaseName(String newName) {
        this.newName = newName;
    }

    @DatabaseChangeProperty(description = "New data retention time in days")
    public String getDataRetentionTimeInDays() {
        return newDataRetentionTimeInDays;
    }

    public void setDataRetentionTimeInDays(String newDataRetentionTimeInDays) {
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
    public String getComment() {
        return newComment;
    }

    public void setComment(String newComment) {
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
        statement.setNewName(getNewDatabaseName());
        statement.setNewDataRetentionTimeInDays(getDataRetentionTimeInDays());
        statement.setNewMaxDataExtensionTimeInDays(getNewMaxDataExtensionTimeInDays());
        statement.setNewDefaultDdlCollation(getNewDefaultDdlCollation());
        statement.setNewComment(getComment());
        statement.setReplaceComment(getReplaceComment());
        statement.setDropComment(getDropComment());
        
        return new SqlStatement[]{statement};
    }

    @Override
    public String getConfirmationMessage() {
        return "Database " + getDatabaseName() + " altered";
    }

    @Override
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
        
        if (getDatabaseName() == null || getDatabaseName().trim().isEmpty()) {
            errors.addError("databaseName is required");
        }
        
        // At least one change must be specified
        if (getNewDatabaseName() == null && 
            getDataRetentionTimeInDays() == null && 
            getNewMaxDataExtensionTimeInDays() == null &&
            getNewDefaultDdlCollation() == null &&
            getComment() == null &&
            (getDropComment() == null || !getDropComment())) {
            errors.addError("At least one database property must be changed");
        }
        
        // Cannot specify both newComment and dropComment
        if (getComment() != null && getDropComment() != null && getDropComment()) {
            errors.addError("Cannot specify both comment and dropComment");
        }
        
        return errors;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return "http://www.liquibase.org/xml/ns/snowflake";
    }
}