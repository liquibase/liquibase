package liquibase.change.core.snowflake;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.snowflake.AlterSchemaStatement;

/**
 * Alters a Snowflake schema.
 */
@DatabaseChange(name = "alterSchema", 
    description = "Alters a Snowflake schema", 
    priority = ChangeMetaData.PRIORITY_DEFAULT)
public class AlterSchemaChange extends AbstractChange {

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

    @DatabaseChangeProperty(description = "Name of the database")
    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

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

    @DatabaseChangeProperty(description = "New comment for the schema")
    public String getNewComment() {
        return newComment;
    }

    public void setNewComment(String newComment) {
        this.newComment = newComment;
    }

    @DatabaseChangeProperty(description = "New data retention time in days")
    public Integer getNewDataRetentionTimeInDays() {
        return newDataRetentionTimeInDays;
    }

    public void setNewDataRetentionTimeInDays(Integer newDataRetentionTimeInDays) {
        this.newDataRetentionTimeInDays = newDataRetentionTimeInDays;
    }

    @DatabaseChangeProperty(description = "New max data extension time in days")
    public Integer getNewMaxDataExtensionTimeInDays() {
        return newMaxDataExtensionTimeInDays;
    }

    public void setNewMaxDataExtensionTimeInDays(Integer newMaxDataExtensionTimeInDays) {
        this.newMaxDataExtensionTimeInDays = newMaxDataExtensionTimeInDays;
    }

    @DatabaseChangeProperty(description = "New default DDL collation")
    public String getNewDefaultDdlCollation() {
        return newDefaultDdlCollation;
    }

    public void setNewDefaultDdlCollation(String newDefaultDdlCollation) {
        this.newDefaultDdlCollation = newDefaultDdlCollation;
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

    @DatabaseChangeProperty(description = "Schema to swap with")
    public String getSwapWith() {
        return swapWith;
    }

    public void setSwapWith(String swapWith) {
        this.swapWith = swapWith;
    }

    @DatabaseChangeProperty(description = "Unset data retention time in days")
    public Boolean getUnsetDataRetentionTimeInDays() {
        return unsetDataRetentionTimeInDays;
    }

    public void setUnsetDataRetentionTimeInDays(Boolean unsetDataRetentionTimeInDays) {
        this.unsetDataRetentionTimeInDays = unsetDataRetentionTimeInDays;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
            new AlterSchemaStatement(
                getDatabaseName(), 
                getSchemaName(), 
                getNewName(),
                getNewComment(),
                getNewDataRetentionTimeInDays(),
                getNewMaxDataExtensionTimeInDays(),
                getNewDefaultDdlCollation(),
                getEnableManagedAccess(),
                getDisableManagedAccess(),
                getSwapWith(),
                getUnsetDataRetentionTimeInDays()
            )
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "Schema " + getSchemaName() + " altered";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return "http://www.liquibase.org/xml/ns/snowflake";
    }
}