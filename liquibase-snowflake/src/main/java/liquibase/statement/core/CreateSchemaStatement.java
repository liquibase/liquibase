package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class CreateSchemaStatement extends AbstractSqlStatement {
    
    private String schemaName;
    private String catalogName;
    private String comment;
    private String dataRetentionTimeInDays;
    private String maxDataExtensionTimeInDays;
    private Boolean transient_;
    private Boolean managed_;
    private String defaultDdlCollation;
    private String pipeExecutionPaused;
    private Boolean orReplace;
    private Boolean ifNotExists;
    private String externalVolume;
    private String cloneFrom;
    private String classificationProfile;
    private String tag;
    private String replaceInvalidCharacters;
    private String storageSerializationPolicy;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
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

    public Boolean getManaged() {
        return managed_;
    }

    public void setManaged(Boolean managed_) {
        this.managed_ = managed_;
    }

    public String getDefaultDdlCollation() {
        return defaultDdlCollation;
    }

    public void setDefaultDdlCollation(String defaultDdlCollation) {
        this.defaultDdlCollation = defaultDdlCollation;
    }

    public String getPipeExecutionPaused() {
        return pipeExecutionPaused;
    }

    public void setPipeExecutionPaused(String pipeExecutionPaused) {
        this.pipeExecutionPaused = pipeExecutionPaused;
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

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public void setCatalog(String catalog) {
        this.catalogName = catalog;
    }

    public String getExternalVolume() {
        return externalVolume;
    }

    public void setExternalVolume(String externalVolume) {
        this.externalVolume = externalVolume;
    }


    public String getCloneFrom() {
        return cloneFrom;
    }

    public void setCloneFrom(String cloneFrom) {
        this.cloneFrom = cloneFrom;
    }

    public String getClassificationProfile() {
        return classificationProfile;
    }

    public void setClassificationProfile(String classificationProfile) {
        this.classificationProfile = classificationProfile;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getReplaceInvalidCharacters() {
        return replaceInvalidCharacters;
    }

    public void setReplaceInvalidCharacters(String replaceInvalidCharacters) {
        this.replaceInvalidCharacters = replaceInvalidCharacters;
    }

    public String getStorageSerializationPolicy() {
        return storageSerializationPolicy;
    }

    public void setStorageSerializationPolicy(String storageSerializationPolicy) {
        this.storageSerializationPolicy = storageSerializationPolicy;
    }
}