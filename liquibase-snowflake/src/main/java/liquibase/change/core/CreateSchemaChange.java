package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
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
    private String catalogName;
    private String comment;
    private String dataRetentionTimeInDays;
    private String maxDataExtensionTimeInDays;
    private Boolean transient_;
    private Boolean managedAccess;
    private String defaultDdlCollation;
    private String pipeExecutionPaused;
    private Boolean orReplace;
    private Boolean ifNotExists;
    private String externalVolume;
    private String catalog;
    private String cloneFrom;
    private String classificationProfile;
    private String tag;
    private String replaceInvalidCharacters;
    private String storageSerializationPolicy;

    @DatabaseChangeProperty(description = "Name of the schema to create", requiredForDatabase = "snowflake")
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

    @DatabaseChangeProperty(description = "Whether this is a managed access schema (alias for managedAccess)")
    public Boolean getManaged() {
        return this.managedAccess;
    }

    @DatabaseChangeProperty(description = "Whether this is a managed access schema (alias for managedAccess)")
    public void setManaged(Boolean managed) {
        this.managedAccess = managed;
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

    @DatabaseChangeProperty(description = "Whether to use CREATE SCHEMA IF NOT EXISTS")
    public Boolean getIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(Boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }


    @DatabaseChangeProperty(description = "Description for externalVolume")
    public String getExternalVolume() {
        return externalVolume;
    }

    public void setExternalVolume(String externalVolume) {
        this.externalVolume = externalVolume;
    }

    @DatabaseChangeProperty(description = "Description for catalog")
    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    @DatabaseChangeProperty(description = "Description for cloneFrom")
    public String getCloneFrom() {
        return cloneFrom;
    }

    public void setCloneFrom(String cloneFrom) {
        this.cloneFrom = cloneFrom;
    }

    @DatabaseChangeProperty(description = "Schema to clone from (alias for cloneFrom)")
    public String getFromSchema() {
        return this.cloneFrom;
    }

    @DatabaseChangeProperty(description = "Schema to clone from (alias for cloneFrom)")
    public void setFromSchema(String fromSchema) {
        this.cloneFrom = fromSchema;
    }

    @DatabaseChangeProperty(description = "Description for classificationProfile")
    public String getClassificationProfile() {
        return classificationProfile;
    }

    public void setClassificationProfile(String classificationProfile) {
        this.classificationProfile = classificationProfile;
    }

    @DatabaseChangeProperty(description = "Description for tag")
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @DatabaseChangeProperty(description = "Description for replaceInvalidCharacters")
    public String getReplaceInvalidCharacters() {
        return replaceInvalidCharacters;
    }

    public void setReplaceInvalidCharacters(String replaceInvalidCharacters) {
        this.replaceInvalidCharacters = replaceInvalidCharacters;
    }

    @DatabaseChangeProperty(description = "Description for storageSerializationPolicy")
    public String getStorageSerializationPolicy() {
        return storageSerializationPolicy;
    }

    public void setStorageSerializationPolicy(String storageSerializationPolicy) {
        this.storageSerializationPolicy = storageSerializationPolicy;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        CreateSchemaStatement statement = new CreateSchemaStatement();
        statement.setSchemaName(getSchemaName());
        
        // Use catalogName for database qualification
        if (getCatalogName() != null) {
            statement.setCatalog(getCatalogName());
        }
        statement.setComment(getComment());
        statement.setDataRetentionTimeInDays(getDataRetentionTimeInDays());
        statement.setMaxDataExtensionTimeInDays(getMaxDataExtensionTimeInDays());
        statement.setTransient(getTransient());
        statement.setManaged(getManagedAccess());
        statement.setDefaultDdlCollation(getDefaultDdlCollation());
        statement.setPipeExecutionPaused(getPipeExecutionPaused());
        statement.setOrReplace(getOrReplace());
        statement.setIfNotExists(getIfNotExists());
        statement.setExternalVolume(getExternalVolume());
        statement.setCatalog(getCatalog());
        statement.setCloneFrom(getCloneFrom());
        statement.setClassificationProfile(getClassificationProfile());
        statement.setTag(getTag());
        statement.setReplaceInvalidCharacters(getReplaceInvalidCharacters());
        statement.setStorageSerializationPolicy(getStorageSerializationPolicy());
        
        return new SqlStatement[]{statement};
    }

    @Override
    public String getConfirmationMessage() {
        return "Schema " + getSchemaName() + " created";
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
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
        
        // Validate that orReplace and ifNotExists are not both set
        if (Boolean.TRUE.equals(getOrReplace()) && Boolean.TRUE.equals(getIfNotExists())) {
            errors.addError("Cannot use both OR REPLACE and IF NOT EXISTS");
        }
        
        return errors;
    }
    
    @Override
    public String getSerializedObjectNamespace() {
        return "http://www.liquibase.org/xml/ns/snowflake";
    }
}