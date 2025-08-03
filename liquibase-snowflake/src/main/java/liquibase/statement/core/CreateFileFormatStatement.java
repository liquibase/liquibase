package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class CreateFileFormatStatement extends AbstractSqlStatement {

    // Core Properties
    private String fileFormatName;
    private String catalogName;
    private String schemaName;
    private String fileFormatType;
    private Boolean orReplace;
    private Boolean ifNotExists;
    private Boolean temporary;
    private Boolean _volatile; // 'volatile' is Java keyword, use _volatile
    private String comment;

    // Common Format Options
    private String compression;
    private String dateFormat;
    private String timeFormat;
    private String timestampFormat;
    private String binaryFormat;
    private Boolean trimSpace;
    private String nullIf;
    private Boolean replaceInvalidCharacters;
    private String fileExtension;

    // CSV-Specific Options
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

    // JSON-Specific Options
    private Boolean enableOctal;
    private Boolean allowDuplicate;
    private Boolean stripOuterArray;
    private Boolean stripNullValues;
    private Boolean ignoreUtf8Errors;

    // Parquet-Specific Options
    private Boolean snappyCompression;
    private Boolean binaryAsText;
    private Boolean useLogicalType;
    private Boolean useVectorizedScanner;

    // XML-Specific Options
    private Boolean preserveSpace;
    private Boolean stripOuterElement;
    private Boolean disableSnowflakeData;
    private Boolean disableAutoConvert;

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

    public String getFileFormatType() {
        return fileFormatType;
    }

    public void setFileFormatType(String fileFormatType) {
        this.fileFormatType = fileFormatType;
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

    public Boolean getTemporary() {
        return temporary;
    }

    public void setTemporary(Boolean temporary) {
        this.temporary = temporary;
    }

    public Boolean getVolatile() {
        return _volatile;
    }

    public void setVolatile(Boolean _volatile) {
        this._volatile = _volatile;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    // Common Format Options Getters/Setters
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

    // CSV-Specific Options Getters/Setters
    public String getRecordDelimiter() {
        return recordDelimiter;
    }

    public void setRecordDelimiter(String recordDelimiter) {
        this.recordDelimiter = recordDelimiter;
    }

    public String getFieldDelimiter() {
        return fieldDelimiter;
    }

    public void setFieldDelimiter(String fieldDelimiter) {
        this.fieldDelimiter = fieldDelimiter;
    }

    public Boolean getParseHeader() {
        return parseHeader;
    }

    public void setParseHeader(Boolean parseHeader) {
        this.parseHeader = parseHeader;
    }

    public Integer getSkipHeader() {
        return skipHeader;
    }

    public void setSkipHeader(Integer skipHeader) {
        this.skipHeader = skipHeader;
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

    public Boolean getValidateUtf8() {
        return validateUtf8;
    }

    public void setValidateUtf8(Boolean validateUtf8) {
        this.validateUtf8 = validateUtf8;
    }

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

    // JSON-Specific Options Getters/Setters
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

    // Parquet-Specific Options Getters/Setters
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

    // XML-Specific Options Getters/Setters
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
}