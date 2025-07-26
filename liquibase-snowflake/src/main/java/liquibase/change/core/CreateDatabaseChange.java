package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateDatabaseStatement;

/**
 * Creates a createDatabase change.
 */
@DatabaseChange(
    name = "createDatabase",
    description = "Creates a database",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "database",
    since = "4.33"
)
public class CreateDatabaseChange extends AbstractChange {

    private String databaseName;
    private String comment;
    private String dataRetentionTimeInDays;
    private String maxDataExtensionTimeInDays;
    private Boolean transient_;
    private String defaultDdlCollation;
    private Boolean orReplace;

    @DatabaseChangeProperty(description = "Name of the database to create", requiredForDatabase = "snowflake")
    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @DatabaseChangeProperty(description = "Comment for the database")
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

    @DatabaseChangeProperty(description = "Whether this is a transient database")
    public Boolean getTransient() {
        return transient_;
    }

    public void setTransient(Boolean transient_) {
        this.transient_ = transient_;
    }

    @DatabaseChangeProperty(description = "Default DDL collation")
    public String getDefaultDdlCollation() {
        return defaultDdlCollation;
    }

    public void setDefaultDdlCollation(String defaultDdlCollation) {
        this.defaultDdlCollation = defaultDdlCollation;
    }

    @DatabaseChangeProperty(description = "Whether to use CREATE OR REPLACE DATABASE")
    public Boolean getOrReplace() {
        return orReplace;
    }

    public void setOrReplace(Boolean orReplace) {
        this.orReplace = orReplace;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        statement.setDatabaseName(getDatabaseName());
        statement.setComment(getComment());
        statement.setDataRetentionTimeInDays(getDataRetentionTimeInDays());
        statement.setMaxDataExtensionTimeInDays(getMaxDataExtensionTimeInDays());
        statement.setTransient(getTransient());
        statement.setDefaultDdlCollation(getDefaultDdlCollation());
        statement.setOrReplace(getOrReplace());
        
        return new SqlStatement[]{statement};
    }

    @Override
    public String getConfirmationMessage() {
        return "Database " + getDatabaseName() + " created";
    }

    @Override
    public boolean supportsRollback(Database database) {
        return true;
    }

    @Override
    public Change[] createInverses() {
        DropDatabaseChange inverse = new DropDatabaseChange();
        inverse.setDatabaseName(getDatabaseName());
        
        return new Change[]{inverse};
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors errors = super.validate(database);
        
        if (getDatabaseName() == null || getDatabaseName().trim().isEmpty()) {
            errors.addError("databaseName is required");
        }
        
        return errors;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return "http://www.liquibase.org/xml/ns/snowflake";
    }
}