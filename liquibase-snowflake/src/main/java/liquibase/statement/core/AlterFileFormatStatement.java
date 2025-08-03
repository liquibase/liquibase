package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class AlterFileFormatStatement extends AbstractSqlStatement {

    // Core Properties
    private String fileFormatName;
    private String catalogName;
    private String schemaName;
    private Boolean ifExists;
    private String newFileFormatName;
    private String operationType;
    
    // SET Operations
    private String newFileFormatType;
    private String newComment;
    
    // All format options for SET operations
    private String compression;
    private String dateFormat;
    private String timeFormat;
    private String timestampFormat;
    private String binaryFormat;
    private Boolean trimSpace;
    private String nullIf;
    private Boolean replaceInvalidCharacters;
    private String fileExtension;
    
    // CSV-specific options
    private String recordDelimiter;
    private String fieldDelimiter;
    private Boolean parseHeader;
    private Integer skipHeader;
    private Boolean skipBlankLines;
    private String escape;
    private String escapeUnenclosedField;
    private String fieldOptionallyEnclosedBy;
    private Boolean errorOnColumnCountMismatch;
    private Boolean validateUtf8;
    private Boolean emptyFieldAsNull;
    private Boolean skipByteOrderMark;
    private String encoding;
    
    // JSON-specific options
    private Boolean enableOctal;
    private Boolean allowDuplicate;
    private Boolean stripOuterArray;
    private Boolean stripNullValues;
    private Boolean ignoreUtf8Errors;
    
    // Parquet-specific options
    private Boolean snappyCompression;
    private Boolean binaryAsText;
    private Boolean useLogicalType;
    private Boolean useVectorizedScanner;
    
    // XML-specific options
    private Boolean preserveSpace;
    private Boolean stripOuterElement;
    private Boolean disableSnowflakeData;
    private Boolean disableAutoConvert;
    
    // UNSET Operations
    private Boolean unsetComment;
    private Boolean unsetCompression;
    private Boolean unsetDateFormat;
    private Boolean unsetTimeFormat;
    private Boolean unsetTimestampFormat;
    private Boolean unsetBinaryFormat;
    private Boolean unsetTrimSpace;
    private Boolean unsetNullIf;
    private Boolean unsetFileExtension;
    private Boolean unsetRecordDelimiter;
    private Boolean unsetFieldDelimiter;
    private Boolean unsetParseHeader;
    private Boolean unsetSkipHeader;
    private Boolean unsetEscape;
    private Boolean unsetFieldOptionallyEnclosedBy;
    private Boolean unsetStripOuterArray;
    private Boolean unsetStripNullValues;
    private Boolean unsetAllowDuplicate;

    // Core Properties Getters/Setters
    public String getFileFormatName() {
        return fileFormatName;
    }

    public void setFileFormatName(String fileFormatName) {
        this.fileFormatName = fileFormatName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public Boolean getIfExists() {
        return ifExists;
    }

    public void setIfExists(Boolean ifExists) {
        this.ifExists = ifExists;
    }

    public String getNewFileFormatName() {
        return newFileFormatName;
    }

    public void setNewFileFormatName(String newFileFormatName) {
        this.newFileFormatName = newFileFormatName;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    // SET Operations Getters/Setters
    public String getNewFileFormatType() {
        return newFileFormatType;
    }

    public void setNewFileFormatType(String newFileFormatType) {
        this.newFileFormatType = newFileFormatType;
    }

    public String getNewComment() {
        return newComment;
    }

    public void setNewComment(String newComment) {
        this.newComment = newComment;
    }

    // Format Options Getters/Setters (all format options from CREATE)
    public String getCompression() {
        return compression;
    }

    public void setCompression(String compression) {
        this.compression = compression;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public String getTimestampFormat() {
        return timestampFormat;
    }

    public void setTimestampFormat(String timestampFormat) {
        this.timestampFormat = timestampFormat;
    }

    public String getBinaryFormat() {
        return binaryFormat;
    }

    public void setBinaryFormat(String binaryFormat) {
        this.binaryFormat = binaryFormat;
    }

    public Boolean getTrimSpace() {
        return trimSpace;
    }

    public void setTrimSpace(Boolean trimSpace) {
        this.trimSpace = trimSpace;
    }

    public String getNullIf() {
        return nullIf;
    }

    public void setNullIf(String nullIf) {
        this.nullIf = nullIf;
    }

    public Boolean getReplaceInvalidCharacters() {
        return replaceInvalidCharacters;
    }

    public void setReplaceInvalidCharacters(Boolean replaceInvalidCharacters) {
        this.replaceInvalidCharacters = replaceInvalidCharacters;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    // UNSET Operations Getters/Setters
    public Boolean getUnsetComment() {
        return unsetComment;
    }

    public void setUnsetComment(Boolean unsetComment) {
        this.unsetComment = unsetComment;
    }

    public Boolean getUnsetCompression() {
        return unsetCompression;
    }

    public void setUnsetCompression(Boolean unsetCompression) {
        this.unsetCompression = unsetCompression;
    }

    public Boolean getUnsetDateFormat() {
        return unsetDateFormat;
    }

    public void setUnsetDateFormat(Boolean unsetDateFormat) {
        this.unsetDateFormat = unsetDateFormat;
    }

    public Boolean getUnsetTimeFormat() {
        return unsetTimeFormat;
    }

    public void setUnsetTimeFormat(Boolean unsetTimeFormat) {
        this.unsetTimeFormat = unsetTimeFormat;
    }

    public Boolean getUnsetTimestampFormat() {
        return unsetTimestampFormat;
    }

    public void setUnsetTimestampFormat(Boolean unsetTimestampFormat) {
        this.unsetTimestampFormat = unsetTimestampFormat;
    }

    public Boolean getUnsetBinaryFormat() {
        return unsetBinaryFormat;
    }

    public void setUnsetBinaryFormat(Boolean unsetBinaryFormat) {
        this.unsetBinaryFormat = unsetBinaryFormat;
    }

    public Boolean getUnsetTrimSpace() {
        return unsetTrimSpace;
    }

    public void setUnsetTrimSpace(Boolean unsetTrimSpace) {
        this.unsetTrimSpace = unsetTrimSpace;
    }

    public Boolean getUnsetNullIf() {
        return unsetNullIf;
    }

    public void setUnsetNullIf(Boolean unsetNullIf) {
        this.unsetNullIf = unsetNullIf;
    }

    public Boolean getUnsetFileExtension() {
        return unsetFileExtension;
    }

    public void setUnsetFileExtension(Boolean unsetFileExtension) {
        this.unsetFileExtension = unsetFileExtension;
    }

    // CSV-specific getters/setters
    public String getFieldDelimiter() {
        return fieldDelimiter;
    }

    public void setFieldDelimiter(String fieldDelimiter) {
        this.fieldDelimiter = fieldDelimiter;
    }

    public Integer getSkipHeader() {
        return skipHeader;
    }

    public void setSkipHeader(Integer skipHeader) {
        this.skipHeader = skipHeader;
    }

    // Additional format option getters/setters would continue here for completeness
}