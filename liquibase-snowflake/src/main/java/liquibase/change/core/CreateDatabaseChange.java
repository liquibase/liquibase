package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;import liquibase.exception.ValidationErrors;
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
    private Boolean ifNotExists;
    private String cloneFrom;
    private String fromDatabase;
    private String tag;
    private String externalVolume;
    private String catalog;
    private Boolean replaceInvalidCharacters;
    private String storageSerializationPolicy;
    private String catalogSync;
    private String catalogSyncNamespaceMode;
    private String catalogSyncNamespaceFlattenDelimiter;

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

    @DatabaseChangeProperty(description = "Whether to use CREATE DATABASE IF NOT EXISTS")
    public Boolean getIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(Boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    @DatabaseChangeProperty(description = "Source database to clone from")
    public String getCloneFrom() {
        return cloneFrom;
    }

    public void setCloneFrom(String cloneFrom) {
        this.cloneFrom = cloneFrom;
    }

    @DatabaseChangeProperty(description = "Alternative source database name for cloning")
    public String getFromDatabase() {
        return fromDatabase;
    }

    public void setFromDatabase(String fromDatabase) {
        this.fromDatabase = fromDatabase;
    }

    @DatabaseChangeProperty(description = "Tag to apply to the database")
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @DatabaseChangeProperty(description = "External volume for Iceberg tables")
    public String getExternalVolume() {
        return externalVolume;
    }

    public void setExternalVolume(String externalVolume) {
        this.externalVolume = externalVolume;
    }

    @DatabaseChangeProperty(description = "Catalog integration for Iceberg tables")
    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    @DatabaseChangeProperty(description = "Replace invalid UTF-8 characters")
    public Boolean getReplaceInvalidCharacters() {
        return replaceInvalidCharacters;
    }

    public void setReplaceInvalidCharacters(Boolean replaceInvalidCharacters) {
        this.replaceInvalidCharacters = replaceInvalidCharacters;
    }

    @DatabaseChangeProperty(description = "Storage serialization policy")
    public String getStorageSerializationPolicy() {
        return storageSerializationPolicy;
    }

    public void setStorageSerializationPolicy(String storageSerializationPolicy) {
        this.storageSerializationPolicy = storageSerializationPolicy;
    }

    @DatabaseChangeProperty(description = "Snowflake Open Catalog integration name")
    public String getCatalogSync() {
        return catalogSync;
    }

    public void setCatalogSync(String catalogSync) {
        this.catalogSync = catalogSync;
    }

    @DatabaseChangeProperty(description = "Catalog sync namespace mode")
    public String getCatalogSyncNamespaceMode() {
        return catalogSyncNamespaceMode;
    }

    public void setCatalogSyncNamespaceMode(String catalogSyncNamespaceMode) {
        this.catalogSyncNamespaceMode = catalogSyncNamespaceMode;
    }

    @DatabaseChangeProperty(description = "Namespace flatten delimiter for catalog sync")
    public String getCatalogSyncNamespaceFlattenDelimiter() {
        return catalogSyncNamespaceFlattenDelimiter;
    }

    public void setCatalogSyncNamespaceFlattenDelimiter(String catalogSyncNamespaceFlattenDelimiter) {
        this.catalogSyncNamespaceFlattenDelimiter = catalogSyncNamespaceFlattenDelimiter;
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
        statement.setIfNotExists(getIfNotExists());
        statement.setCloneFrom(getCloneFrom());
        statement.setFromDatabase(getFromDatabase());
        statement.setTag(getTag());
        statement.setExternalVolume(getExternalVolume());
        statement.setCatalog(getCatalog());
        statement.setReplaceInvalidCharacters(getReplaceInvalidCharacters());
        statement.setStorageSerializationPolicy(getStorageSerializationPolicy());
        statement.setCatalogSync(getCatalogSync());
        statement.setCatalogSyncNamespaceMode(getCatalogSyncNamespaceMode());
        statement.setCatalogSyncNamespaceFlattenDelimiter(getCatalogSyncNamespaceFlattenDelimiter());
        
        return new SqlStatement[]{statement};
    }

    @Override
    public String getConfirmationMessage() {
        return "Database " + getDatabaseName() + " created";
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override    public boolean supportsRollback(Database database) {
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
        
        // Validate that orReplace and ifNotExists are not both set
        if (Boolean.TRUE.equals(getOrReplace()) && Boolean.TRUE.equals(getIfNotExists())) {
            errors.addError("Cannot use both OR REPLACE and IF NOT EXISTS");
        }
        
        // Validate transient databases must have 0 retention time
        if (Boolean.TRUE.equals(getTransient()) && getDataRetentionTimeInDays() != null) {
            try {
                int days = Integer.parseInt(getDataRetentionTimeInDays());
                if (days > 0) {
                    errors.addError("Transient databases must have DATA_RETENTION_TIME_IN_DAYS = 0");
                }
            } catch (NumberFormatException e) {
                errors.addError("Invalid dataRetentionTimeInDays value: " + getDataRetentionTimeInDays());
            }
        }
        
        return errors;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return "http://www.liquibase.org/xml/ns/snowflake";
    }
}