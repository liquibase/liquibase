package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class CreateDatabaseStatement extends AbstractSqlStatement {
    
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

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDataRetentionTimeInDays() {
        return dataRetentionTimeInDays;
    }

    public void setDataRetentionTimeInDays(String dataRetentionTimeInDays) {
        this.dataRetentionTimeInDays = dataRetentionTimeInDays;
    }

    public String getMaxDataExtensionTimeInDays() {
        return maxDataExtensionTimeInDays;
    }

    public void setMaxDataExtensionTimeInDays(String maxDataExtensionTimeInDays) {
        this.maxDataExtensionTimeInDays = maxDataExtensionTimeInDays;
    }

    public Boolean getTransient() {
        return transient_;
    }

    public void setTransient(Boolean transient_) {
        this.transient_ = transient_;
    }

    public String getDefaultDdlCollation() {
        return defaultDdlCollation;
    }

    public void setDefaultDdlCollation(String defaultDdlCollation) {
        this.defaultDdlCollation = defaultDdlCollation;
    }

    public Boolean getOrReplace() {
        return orReplace;
    }

    public void setOrReplace(Boolean orReplace) {
        this.orReplace = orReplace;
    }

    public Boolean getIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(Boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    public String getCloneFrom() {
        return cloneFrom;
    }

    public void setCloneFrom(String cloneFrom) {
        this.cloneFrom = cloneFrom;
    }

    public String getFromDatabase() {
        return fromDatabase;
    }

    public void setFromDatabase(String fromDatabase) {
        this.fromDatabase = fromDatabase;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getExternalVolume() {
        return externalVolume;
    }

    public void setExternalVolume(String externalVolume) {
        this.externalVolume = externalVolume;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public Boolean getReplaceInvalidCharacters() {
        return replaceInvalidCharacters;
    }

    public void setReplaceInvalidCharacters(Boolean replaceInvalidCharacters) {
        this.replaceInvalidCharacters = replaceInvalidCharacters;
    }

    public String getStorageSerializationPolicy() {
        return storageSerializationPolicy;
    }

    public void setStorageSerializationPolicy(String storageSerializationPolicy) {
        this.storageSerializationPolicy = storageSerializationPolicy;
    }

    public String getCatalogSync() {
        return catalogSync;
    }

    public void setCatalogSync(String catalogSync) {
        this.catalogSync = catalogSync;
    }

    public String getCatalogSyncNamespaceMode() {
        return catalogSyncNamespaceMode;
    }

    public void setCatalogSyncNamespaceMode(String catalogSyncNamespaceMode) {
        this.catalogSyncNamespaceMode = catalogSyncNamespaceMode;
    }

    public String getCatalogSyncNamespaceFlattenDelimiter() {
        return catalogSyncNamespaceFlattenDelimiter;
    }

    public void setCatalogSyncNamespaceFlattenDelimiter(String catalogSyncNamespaceFlattenDelimiter) {
        this.catalogSyncNamespaceFlattenDelimiter = catalogSyncNamespaceFlattenDelimiter;
    }
}