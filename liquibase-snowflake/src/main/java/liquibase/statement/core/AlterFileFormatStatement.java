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
    private String currentFileFormatType; // Current format type for change detection
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
    // validateUtf8 removed - not available in Snowflake INFORMATION_SCHEMA.FILE_FORMATS
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
    
    // UNSET Operations - Complete set
    private Boolean unsetComment;
    private Boolean unsetCompression;
    private Boolean unsetDateFormat;
    private Boolean unsetTimeFormat;
    private Boolean unsetTimestampFormat;
    private Boolean unsetBinaryFormat;
    private Boolean unsetTrimSpace;
    private Boolean unsetNullIf;
    private Boolean unsetFileExtension;
    private Boolean unsetReplaceInvalidCharacters;
    
    // CSV-specific UNSET operations
    private Boolean unsetRecordDelimiter;
    private Boolean unsetFieldDelimiter;
    private Boolean unsetParseHeader;
    private Boolean unsetSkipHeader;
    private Boolean unsetSkipBlankLines;
    private Boolean unsetEscape;
    private Boolean unsetEscapeUnenclosedField;
    private Boolean unsetFieldOptionallyEnclosedBy;
    private Boolean unsetErrorOnColumnCountMismatch;
    // unsetValidateUtf8 removed - not available in Snowflake INFORMATION_SCHEMA.FILE_FORMATS
    private Boolean unsetEmptyFieldAsNull;
    private Boolean unsetSkipByteOrderMark;
    private Boolean unsetEncoding;
    
    // JSON-specific UNSET operations
    private Boolean unsetEnableOctal;
    private Boolean unsetAllowDuplicate;
    private Boolean unsetStripOuterArray;
    private Boolean unsetStripNullValues;
    private Boolean unsetIgnoreUtf8Errors;
    
    // PARQUET-specific UNSET operations
    private Boolean unsetSnappyCompression;
    private Boolean unsetBinaryAsText;
    private Boolean unsetUseLogicalType;
    private Boolean unsetUseVectorizedScanner;
    
    // XML-specific UNSET operations
    private Boolean unsetPreserveSpace;
    private Boolean unsetStripOuterElement;
    private Boolean unsetDisableSnowflakeData;
    private Boolean unsetDisableAutoConvert;

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
    public String getCurrentFileFormatType() {
        return currentFileFormatType;
    }

    public void setCurrentFileFormatType(String currentFileFormatType) {
        this.currentFileFormatType = currentFileFormatType;
    }

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

    // Additional format-specific getters/setters
    public String getRecordDelimiter() {
        return recordDelimiter;
    }

    public void setRecordDelimiter(String recordDelimiter) {
        this.recordDelimiter = recordDelimiter;
    }

    public Boolean getParseHeader() {
        return parseHeader;
    }

    public void setParseHeader(Boolean parseHeader) {
        this.parseHeader = parseHeader;
    }

    public Boolean getSkipBlankLines() {
        return skipBlankLines;
    }

    public void setSkipBlankLines(Boolean skipBlankLines) {
        this.skipBlankLines = skipBlankLines;
    }

    public String getEscape() {
        return escape;
    }

    public void setEscape(String escape) {
        this.escape = escape;
    }

    public String getEscapeUnenclosedField() {
        return escapeUnenclosedField;
    }

    public void setEscapeUnenclosedField(String escapeUnenclosedField) {
        this.escapeUnenclosedField = escapeUnenclosedField;
    }

    public String getFieldOptionallyEnclosedBy() {
        return fieldOptionallyEnclosedBy;
    }

    public void setFieldOptionallyEnclosedBy(String fieldOptionallyEnclosedBy) {
        this.fieldOptionallyEnclosedBy = fieldOptionallyEnclosedBy;
    }

    public Boolean getErrorOnColumnCountMismatch() {
        return errorOnColumnCountMismatch;
    }

    public void setErrorOnColumnCountMismatch(Boolean errorOnColumnCountMismatch) {
        this.errorOnColumnCountMismatch = errorOnColumnCountMismatch;
    }

    // validateUtf8 getter/setter removed - not available in Snowflake INFORMATION_SCHEMA.FILE_FORMATS

    public Boolean getEmptyFieldAsNull() {
        return emptyFieldAsNull;
    }

    public void setEmptyFieldAsNull(Boolean emptyFieldAsNull) {
        this.emptyFieldAsNull = emptyFieldAsNull;
    }

    public Boolean getSkipByteOrderMark() {
        return skipByteOrderMark;
    }

    public void setSkipByteOrderMark(Boolean skipByteOrderMark) {
        this.skipByteOrderMark = skipByteOrderMark;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    // JSON-specific getters/setters
    public Boolean getEnableOctal() {
        return enableOctal;
    }

    public void setEnableOctal(Boolean enableOctal) {
        this.enableOctal = enableOctal;
    }

    public Boolean getAllowDuplicate() {
        return allowDuplicate;
    }

    public void setAllowDuplicate(Boolean allowDuplicate) {
        this.allowDuplicate = allowDuplicate;
    }

    public Boolean getStripOuterArray() {
        return stripOuterArray;
    }

    public void setStripOuterArray(Boolean stripOuterArray) {
        this.stripOuterArray = stripOuterArray;
    }

    public Boolean getStripNullValues() {
        return stripNullValues;
    }

    public void setStripNullValues(Boolean stripNullValues) {
        this.stripNullValues = stripNullValues;
    }

    public Boolean getIgnoreUtf8Errors() {
        return ignoreUtf8Errors;
    }

    public void setIgnoreUtf8Errors(Boolean ignoreUtf8Errors) {
        this.ignoreUtf8Errors = ignoreUtf8Errors;
    }

    // PARQUET-specific getters/setters
    public Boolean getSnappyCompression() {
        return snappyCompression;
    }

    public void setSnappyCompression(Boolean snappyCompression) {
        this.snappyCompression = snappyCompression;
    }

    public Boolean getBinaryAsText() {
        return binaryAsText;
    }

    public void setBinaryAsText(Boolean binaryAsText) {
        this.binaryAsText = binaryAsText;
    }

    public Boolean getUseLogicalType() {
        return useLogicalType;
    }

    public void setUseLogicalType(Boolean useLogicalType) {
        this.useLogicalType = useLogicalType;
    }

    public Boolean getUseVectorizedScanner() {
        return useVectorizedScanner;
    }

    public void setUseVectorizedScanner(Boolean useVectorizedScanner) {
        this.useVectorizedScanner = useVectorizedScanner;
    }

    // XML-specific getters/setters
    public Boolean getPreserveSpace() {
        return preserveSpace;
    }

    public void setPreserveSpace(Boolean preserveSpace) {
        this.preserveSpace = preserveSpace;
    }

    public Boolean getStripOuterElement() {
        return stripOuterElement;
    }

    public void setStripOuterElement(Boolean stripOuterElement) {
        this.stripOuterElement = stripOuterElement;
    }

    public Boolean getDisableSnowflakeData() {
        return disableSnowflakeData;
    }

    public void setDisableSnowflakeData(Boolean disableSnowflakeData) {
        this.disableSnowflakeData = disableSnowflakeData;
    }

    public Boolean getDisableAutoConvert() {
        return disableAutoConvert;
    }

    public void setDisableAutoConvert(Boolean disableAutoConvert) {
        this.disableAutoConvert = disableAutoConvert;
    }

    // Additional UNSET getters/setters
    public Boolean getUnsetReplaceInvalidCharacters() {
        return unsetReplaceInvalidCharacters;
    }

    public void setUnsetReplaceInvalidCharacters(Boolean unsetReplaceInvalidCharacters) {
        this.unsetReplaceInvalidCharacters = unsetReplaceInvalidCharacters;
    }

    public Boolean getUnsetRecordDelimiter() {
        return unsetRecordDelimiter;
    }

    public void setUnsetRecordDelimiter(Boolean unsetRecordDelimiter) {
        this.unsetRecordDelimiter = unsetRecordDelimiter;
    }

    public Boolean getUnsetFieldDelimiter() {
        return unsetFieldDelimiter;
    }

    public void setUnsetFieldDelimiter(Boolean unsetFieldDelimiter) {
        this.unsetFieldDelimiter = unsetFieldDelimiter;
    }

    public Boolean getUnsetParseHeader() {
        return unsetParseHeader;
    }

    public void setUnsetParseHeader(Boolean unsetParseHeader) {
        this.unsetParseHeader = unsetParseHeader;
    }

    public Boolean getUnsetSkipHeader() {
        return unsetSkipHeader;
    }

    public void setUnsetSkipHeader(Boolean unsetSkipHeader) {
        this.unsetSkipHeader = unsetSkipHeader;
    }

    public Boolean getUnsetSkipBlankLines() {
        return unsetSkipBlankLines;
    }

    public void setUnsetSkipBlankLines(Boolean unsetSkipBlankLines) {
        this.unsetSkipBlankLines = unsetSkipBlankLines;
    }

    public Boolean getUnsetEscape() {
        return unsetEscape;
    }

    public void setUnsetEscape(Boolean unsetEscape) {
        this.unsetEscape = unsetEscape;
    }

    public Boolean getUnsetEscapeUnenclosedField() {
        return unsetEscapeUnenclosedField;
    }

    public void setUnsetEscapeUnenclosedField(Boolean unsetEscapeUnenclosedField) {
        this.unsetEscapeUnenclosedField = unsetEscapeUnenclosedField;
    }

    public Boolean getUnsetFieldOptionallyEnclosedBy() {
        return unsetFieldOptionallyEnclosedBy;
    }

    public void setUnsetFieldOptionallyEnclosedBy(Boolean unsetFieldOptionallyEnclosedBy) {
        this.unsetFieldOptionallyEnclosedBy = unsetFieldOptionallyEnclosedBy;
    }

    public Boolean getUnsetErrorOnColumnCountMismatch() {
        return unsetErrorOnColumnCountMismatch;
    }

    public void setUnsetErrorOnColumnCountMismatch(Boolean unsetErrorOnColumnCountMismatch) {
        this.unsetErrorOnColumnCountMismatch = unsetErrorOnColumnCountMismatch;
    }

    // unsetValidateUtf8 getter/setter removed - not available in Snowflake INFORMATION_SCHEMA.FILE_FORMATS

    public Boolean getUnsetEmptyFieldAsNull() {
        return unsetEmptyFieldAsNull;
    }

    public void setUnsetEmptyFieldAsNull(Boolean unsetEmptyFieldAsNull) {
        this.unsetEmptyFieldAsNull = unsetEmptyFieldAsNull;
    }

    public Boolean getUnsetSkipByteOrderMark() {
        return unsetSkipByteOrderMark;
    }

    public void setUnsetSkipByteOrderMark(Boolean unsetSkipByteOrderMark) {
        this.unsetSkipByteOrderMark = unsetSkipByteOrderMark;
    }

    public Boolean getUnsetEncoding() {
        return unsetEncoding;
    }

    public void setUnsetEncoding(Boolean unsetEncoding) {
        this.unsetEncoding = unsetEncoding;
    }

    // JSON-specific UNSET getters/setters
    public Boolean getUnsetEnableOctal() {
        return unsetEnableOctal;
    }

    public void setUnsetEnableOctal(Boolean unsetEnableOctal) {
        this.unsetEnableOctal = unsetEnableOctal;
    }

    public Boolean getUnsetAllowDuplicate() {
        return unsetAllowDuplicate;
    }

    public void setUnsetAllowDuplicate(Boolean unsetAllowDuplicate) {
        this.unsetAllowDuplicate = unsetAllowDuplicate;
    }

    public Boolean getUnsetStripOuterArray() {
        return unsetStripOuterArray;
    }

    public void setUnsetStripOuterArray(Boolean unsetStripOuterArray) {
        this.unsetStripOuterArray = unsetStripOuterArray;
    }

    public Boolean getUnsetStripNullValues() {
        return unsetStripNullValues;
    }

    public void setUnsetStripNullValues(Boolean unsetStripNullValues) {
        this.unsetStripNullValues = unsetStripNullValues;
    }

    public Boolean getUnsetIgnoreUtf8Errors() {
        return unsetIgnoreUtf8Errors;
    }

    public void setUnsetIgnoreUtf8Errors(Boolean unsetIgnoreUtf8Errors) {
        this.unsetIgnoreUtf8Errors = unsetIgnoreUtf8Errors;
    }

    // PARQUET-specific UNSET getters/setters
    public Boolean getUnsetSnappyCompression() {
        return unsetSnappyCompression;
    }

    public void setUnsetSnappyCompression(Boolean unsetSnappyCompression) {
        this.unsetSnappyCompression = unsetSnappyCompression;
    }

    public Boolean getUnsetBinaryAsText() {
        return unsetBinaryAsText;
    }

    public void setUnsetBinaryAsText(Boolean unsetBinaryAsText) {
        this.unsetBinaryAsText = unsetBinaryAsText;
    }

    public Boolean getUnsetUseLogicalType() {
        return unsetUseLogicalType;
    }

    public void setUnsetUseLogicalType(Boolean unsetUseLogicalType) {
        this.unsetUseLogicalType = unsetUseLogicalType;
    }

    public Boolean getUnsetUseVectorizedScanner() {
        return unsetUseVectorizedScanner;
    }

    public void setUnsetUseVectorizedScanner(Boolean unsetUseVectorizedScanner) {
        this.unsetUseVectorizedScanner = unsetUseVectorizedScanner;
    }

    // XML-specific UNSET getters/setters
    public Boolean getUnsetPreserveSpace() {
        return unsetPreserveSpace;
    }

    public void setUnsetPreserveSpace(Boolean unsetPreserveSpace) {
        this.unsetPreserveSpace = unsetPreserveSpace;
    }

    public Boolean getUnsetStripOuterElement() {
        return unsetStripOuterElement;
    }

    public void setUnsetStripOuterElement(Boolean unsetStripOuterElement) {
        this.unsetStripOuterElement = unsetStripOuterElement;
    }

    public Boolean getUnsetDisableSnowflakeData() {
        return unsetDisableSnowflakeData;
    }

    public void setUnsetDisableSnowflakeData(Boolean unsetDisableSnowflakeData) {
        this.unsetDisableSnowflakeData = unsetDisableSnowflakeData;
    }

    public Boolean getUnsetDisableAutoConvert() {
        return unsetDisableAutoConvert;
    }

    public void setUnsetDisableAutoConvert(Boolean unsetDisableAutoConvert) {
        this.unsetDisableAutoConvert = unsetDisableAutoConvert;
    }
}