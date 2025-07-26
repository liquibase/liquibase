package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateSchemaStatement;

/**
 * Creates a createSchema change.
 */
@DatabaseChange(
    name = "createSchema",
    description = "Creates a schema",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "schema",
    since = "4.33"
)
public class CreateSchemaChange extends AbstractChange {

    private String schemaName;
    private String comment;
    private String dataRetentionTimeInDays;
    private String maxDataExtensionTimeInDays;
    private Boolean transient_;
    private Boolean managedAccess;
    private String defaultDdlCollation;
    private String pipeExecutionPaused;
    private Boolean orReplace;

    @DatabaseChangeProperty(description = "Name of the schema to create", requiredForDatabase = "snowflake")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(description = "Comment for the schema")
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @DatabaseChangeProperty(description = "Data retention time in days")
    public String getDataRetentionTimeInDays() {
        return dataRetentionTimeInDays;
    }

    public void setDataRetentionTimeInDays(String dataRetentionTimeInDays) {
        this.dataRetentionTimeInDays = dataRetentionTimeInDays;
    }

    @DatabaseChangeProperty(description = "Maximum data extension time in days")
    public String getMaxDataExtensionTimeInDays() {
        return maxDataExtensionTimeInDays;
    }

    public void setMaxDataExtensionTimeInDays(String maxDataExtensionTimeInDays) {
        this.maxDataExtensionTimeInDays = maxDataExtensionTimeInDays;
    }

    @DatabaseChangeProperty(description = "Whether this is a transient schema")
    public Boolean getTransient() {
        return transient_;
    }

    public void setTransient(Boolean transient_) {
        this.transient_ = transient_;
    }

    @DatabaseChangeProperty(description = "Whether this is a managed access schema")
    public Boolean getManagedAccess() {
        return managedAccess;
    }

    public void setManagedAccess(Boolean managedAccess) {
        this.managedAccess = managedAccess;
    }

    @DatabaseChangeProperty(description = "Default DDL collation")
    public String getDefaultDdlCollation() {
        return defaultDdlCollation;
    }

    public void setDefaultDdlCollation(String defaultDdlCollation) {
        this.defaultDdlCollation = defaultDdlCollation;
    }

    @DatabaseChangeProperty(description = "Whether pipe execution is paused")
    public String getPipeExecutionPaused() {
        return pipeExecutionPaused;
    }

    public void setPipeExecutionPaused(String pipeExecutionPaused) {
        this.pipeExecutionPaused = pipeExecutionPaused;
    }

    @DatabaseChangeProperty(description = "Whether to use CREATE OR REPLACE SCHEMA")
    public Boolean getOrReplace() {
        return orReplace;
    }

    public void setOrReplace(Boolean orReplace) {
        this.orReplace = orReplace;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        CreateSchemaStatement statement = new CreateSchemaStatement();
        statement.setSchemaName(getSchemaName());
        statement.setComment(getComment());
        statement.setDataRetentionTimeInDays(getDataRetentionTimeInDays());
        statement.setMaxDataExtensionTimeInDays(getMaxDataExtensionTimeInDays());
        statement.setTransient(getTransient());
        statement.setManaged(getManagedAccess());
        statement.setDefaultDdlCollation(getDefaultDdlCollation());
        statement.setPipeExecutionPaused(getPipeExecutionPaused());
        statement.setOrReplace(getOrReplace());
        
        return new SqlStatement[]{statement};
    }

    @Override
    public String getConfirmationMessage() {
        return "Schema " + getSchemaName() + " created";
    }

    @Override
    public boolean supportsRollback(Database database) {
        return true;
    }

    @Override
    public Change[] createInverses() {
        DropSchemaChange inverse = new DropSchemaChange();
        inverse.setSchemaName(getSchemaName());
        
        return new Change[]{inverse};
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors errors = super.validate(database);
        
        if (getSchemaName() == null || getSchemaName().trim().isEmpty()) {
            errors.addError("schemaName is required");
        }
        
        return errors;
    }
    
    @Override
    public String getSerializedObjectNamespace() {
        return "http://www.liquibase.org/xml/ns/snowflake";
    }
}