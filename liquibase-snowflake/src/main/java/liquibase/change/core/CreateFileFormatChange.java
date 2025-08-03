package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateFileFormatStatement;

/**
 * Creates a new file format in Snowflake.
 */
@DatabaseChange(
    name = "createFileFormat",
    description = "Creates a file format",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "fileFormat",
    since = "4.33"
)
public class CreateFileFormatChange extends AbstractChange {

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
    @DatabaseChangeProperty(description = "Name of the file format to create", requiredForDatabase = "snowflake")
    public String getFileFormatName() {
        return fileFormatName;
    }

    public void setFileFormatName(String fileFormatName) {
        this.fileFormatName = fileFormatName;
    }

    @DatabaseChangeProperty(description = "Catalog (database) name")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(description = "Schema name")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(description = "Type of file format (CSV, JSON, AVRO, ORC, PARQUET, XML, CUSTOM)")
    public String getFileFormatType() {
        return fileFormatType;
    }

    public void setFileFormatType(String fileFormatType) {
        this.fileFormatType = fileFormatType;
    }

    @DatabaseChangeProperty(description = "Use CREATE OR REPLACE FILE FORMAT")
    public Boolean getOrReplace() {
        return orReplace;
    }

    public void setOrReplace(Boolean orReplace) {
        this.orReplace = orReplace;
    }

    @DatabaseChangeProperty(description = "Use CREATE FILE FORMAT IF NOT EXISTS")
    public Boolean getIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(Boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    @DatabaseChangeProperty(description = "Create temporary file format")
    public Boolean getTemporary() {
        return temporary;
    }

    public void setTemporary(Boolean temporary) {
        this.temporary = temporary;
    }

    @DatabaseChangeProperty(description = "Create volatile file format")
    public Boolean getVolatile() {
        return _volatile;
    }

    public void setVolatile(Boolean _volatile) {
        this._volatile = _volatile;
    }

    @DatabaseChangeProperty(description = "Comment for the file format")
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    // Common Format Options Getters/Setters
    @DatabaseChangeProperty(description = "Compression algorithm")
    public String getCompression() {
        return compression;
    }

    public void setCompression(String compression) {
        this.compression = compression;
    }

    @DatabaseChangeProperty(description = "Date format string")
    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    @DatabaseChangeProperty(description = "Time format string")
    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    @DatabaseChangeProperty(description = "Timestamp format string")
    public String getTimestampFormat() {
        return timestampFormat;
    }

    public void setTimestampFormat(String timestampFormat) {
        this.timestampFormat = timestampFormat;
    }

    @DatabaseChangeProperty(description = "Binary data format (HEX, BASE64, UTF8)")
    public String getBinaryFormat() {
        return binaryFormat;
    }

    public void setBinaryFormat(String binaryFormat) {
        this.binaryFormat = binaryFormat;
    }

    @DatabaseChangeProperty(description = "Trim leading/trailing spaces")
    public Boolean getTrimSpace() {
        return trimSpace;
    }

    public void setTrimSpace(Boolean trimSpace) {
        this.trimSpace = trimSpace;
    }

    @DatabaseChangeProperty(description = "Strings to convert to NULL")
    public String getNullIf() {
        return nullIf;
    }

    public void setNullIf(String nullIf) {
        this.nullIf = nullIf;
    }

    @DatabaseChangeProperty(description = "Replace invalid UTF-8 characters")
    public Boolean getReplaceInvalidCharacters() {
        return replaceInvalidCharacters;
    }

    public void setReplaceInvalidCharacters(Boolean replaceInvalidCharacters) {
        this.replaceInvalidCharacters = replaceInvalidCharacters;
    }

    @DatabaseChangeProperty(description = "Expected file extension")
    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    // CSV-Specific Options Getters/Setters
    @DatabaseChangeProperty(description = "Record delimiter for CSV files")
    public String getRecordDelimiter() {
        return recordDelimiter;
    }

    public void setRecordDelimiter(String recordDelimiter) {
        this.recordDelimiter = recordDelimiter;
    }

    @DatabaseChangeProperty(description = "Field delimiter for CSV files")
    public String getFieldDelimiter() {
        return fieldDelimiter;
    }

    public void setFieldDelimiter(String fieldDelimiter) {
        this.fieldDelimiter = fieldDelimiter;
    }

    @DatabaseChangeProperty(description = "Parse first line as header")
    public Boolean getParseHeader() {
        return parseHeader;
    }

    public void setParseHeader(Boolean parseHeader) {
        this.parseHeader = parseHeader;
    }

    @DatabaseChangeProperty(description = "Number of header lines to skip")
    public Integer getSkipHeader() {
        return skipHeader;
    }

    public void setSkipHeader(Integer skipHeader) {
        this.skipHeader = skipHeader;
    }

    @DatabaseChangeProperty(description = "Skip blank lines")
    public Boolean getSkipBlankLines() {
        return skipBlankLines;
    }

    public void setSkipBlankLines(Boolean skipBlankLines) {
        this.skipBlankLines = skipBlankLines;
    }

    @DatabaseChangeProperty(description = "Escape character")
    public String getEscape() {
        return escape;
    }

    public void setEscape(String escape) {
        this.escape = escape;
    }

    @DatabaseChangeProperty(description = "Escape character for unenclosed fields")
    public String getEscapeUnenclosedField() {
        return escapeUnenclosedField;
    }

    public void setEscapeUnenclosedField(String escapeUnenclosedField) {
        this.escapeUnenclosedField = escapeUnenclosedField;
    }

    @DatabaseChangeProperty(description = "Field enclosure character")
    public String getFieldOptionallyEnclosedBy() {
        return fieldOptionallyEnclosedBy;
    }

    public void setFieldOptionallyEnclosedBy(String fieldOptionallyEnclosedBy) {
        this.fieldOptionallyEnclosedBy = fieldOptionallyEnclosedBy;
    }

    @DatabaseChangeProperty(description = "Error on column count mismatch")
    public Boolean getErrorOnColumnCountMismatch() {
        return errorOnColumnCountMismatch;
    }

    public void setErrorOnColumnCountMismatch(Boolean errorOnColumnCountMismatch) {
        this.errorOnColumnCountMismatch = errorOnColumnCountMismatch;
    }

    @DatabaseChangeProperty(description = "Validate UTF-8 encoding")
    public Boolean getValidateUtf8() {
        return validateUtf8;
    }

    public void setValidateUtf8(Boolean validateUtf8) {
        this.validateUtf8 = validateUtf8;
    }

    @DatabaseChangeProperty(description = "Treat empty fields as NULL")
    public Boolean getEmptyFieldAsNull() {
        return emptyFieldAsNull;
    }

    public void setEmptyFieldAsNull(Boolean emptyFieldAsNull) {
        this.emptyFieldAsNull = emptyFieldAsNull;
    }

    @DatabaseChangeProperty(description = "Skip byte order mark")
    public Boolean getSkipByteOrderMark() {
        return skipByteOrderMark;
    }

    public void setSkipByteOrderMark(Boolean skipByteOrderMark) {
        this.skipByteOrderMark = skipByteOrderMark;
    }

    @DatabaseChangeProperty(description = "Character encoding")
    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    // JSON-Specific Options Getters/Setters
    @DatabaseChangeProperty(description = "Allow octal values")
    public Boolean getEnableOctal() {
        return enableOctal;
    }

    public void setEnableOctal(Boolean enableOctal) {
        this.enableOctal = enableOctal;
    }

    @DatabaseChangeProperty(description = "Allow duplicate field names")
    public Boolean getAllowDuplicate() {
        return allowDuplicate;
    }

    public void setAllowDuplicate(Boolean allowDuplicate) {
        this.allowDuplicate = allowDuplicate;
    }

    @DatabaseChangeProperty(description = "Strip outer array")
    public Boolean getStripOuterArray() {
        return stripOuterArray;
    }

    public void setStripOuterArray(Boolean stripOuterArray) {
        this.stripOuterArray = stripOuterArray;
    }

    @DatabaseChangeProperty(description = "Strip null values")
    public Boolean getStripNullValues() {
        return stripNullValues;
    }

    public void setStripNullValues(Boolean stripNullValues) {
        this.stripNullValues = stripNullValues;
    }

    @DatabaseChangeProperty(description = "Ignore UTF-8 errors")
    public Boolean getIgnoreUtf8Errors() {
        return ignoreUtf8Errors;
    }

    public void setIgnoreUtf8Errors(Boolean ignoreUtf8Errors) {
        this.ignoreUtf8Errors = ignoreUtf8Errors;
    }

    // Parquet-Specific Options Getters/Setters
    @DatabaseChangeProperty(description = "Use Snappy compression")
    public Boolean getSnappyCompression() {
        return snappyCompression;
    }

    public void setSnappyCompression(Boolean snappyCompression) {
        this.snappyCompression = snappyCompression;
    }

    @DatabaseChangeProperty(description = "Interpret binary columns as text")
    public Boolean getBinaryAsText() {
        return binaryAsText;
    }

    public void setBinaryAsText(Boolean binaryAsText) {
        this.binaryAsText = binaryAsText;
    }

    @DatabaseChangeProperty(description = "Use Parquet logical types")
    public Boolean getUseLogicalType() {
        return useLogicalType;
    }

    public void setUseLogicalType(Boolean useLogicalType) {
        this.useLogicalType = useLogicalType;
    }

    @DatabaseChangeProperty(description = "Use vectorized scanner")
    public Boolean getUseVectorizedScanner() {
        return useVectorizedScanner;
    }

    public void setUseVectorizedScanner(Boolean useVectorizedScanner) {
        this.useVectorizedScanner = useVectorizedScanner;
    }

    // XML-Specific Options Getters/Setters
    @DatabaseChangeProperty(description = "Preserve whitespace")
    public Boolean getPreserveSpace() {
        return preserveSpace;
    }

    public void setPreserveSpace(Boolean preserveSpace) {
        this.preserveSpace = preserveSpace;
    }

    @DatabaseChangeProperty(description = "Strip outer element")
    public Boolean getStripOuterElement() {
        return stripOuterElement;
    }

    public void setStripOuterElement(Boolean stripOuterElement) {
        this.stripOuterElement = stripOuterElement;
    }

    @DatabaseChangeProperty(description = "Disable Snowflake data")
    public Boolean getDisableSnowflakeData() {
        return disableSnowflakeData;
    }

    public void setDisableSnowflakeData(Boolean disableSnowflakeData) {
        this.disableSnowflakeData = disableSnowflakeData;
    }

    @DatabaseChangeProperty(description = "Disable automatic conversion")
    public Boolean getDisableAutoConvert() {
        return disableAutoConvert;
    }

    public void setDisableAutoConvert(Boolean disableAutoConvert) {
        this.disableAutoConvert = disableAutoConvert;
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        
        // Core properties
        statement.setFileFormatName(getFileFormatName());
        statement.setCatalogName(getCatalogName());
        statement.setSchemaName(getSchemaName());
        statement.setFileFormatType(getFileFormatType());
        statement.setOrReplace(getOrReplace());
        statement.setIfNotExists(getIfNotExists());
        statement.setTemporary(getTemporary());
        statement.setVolatile(getVolatile());
        statement.setComment(getComment());

        // Common format options
        statement.setCompression(getCompression());
        statement.setDateFormat(getDateFormat());
        statement.setTimeFormat(getTimeFormat());
        statement.setTimestampFormat(getTimestampFormat());
        statement.setBinaryFormat(getBinaryFormat());
        statement.setTrimSpace(getTrimSpace());
        statement.setNullIf(getNullIf());
        statement.setReplaceInvalidCharacters(getReplaceInvalidCharacters());
        statement.setFileExtension(getFileExtension());

        // CSV-specific options
        statement.setRecordDelimiter(getRecordDelimiter());
        statement.setFieldDelimiter(getFieldDelimiter());
        statement.setParseHeader(getParseHeader());
        statement.setSkipHeader(getSkipHeader());
        statement.setSkipBlankLines(getSkipBlankLines());
        statement.setEscape(getEscape());
        statement.setEscapeUnenclosedField(getEscapeUnenclosedField());
        statement.setFieldOptionallyEnclosedBy(getFieldOptionallyEnclosedBy());
        statement.setErrorOnColumnCountMismatch(getErrorOnColumnCountMismatch());
        statement.setValidateUtf8(getValidateUtf8());
        statement.setEmptyFieldAsNull(getEmptyFieldAsNull());
        statement.setSkipByteOrderMark(getSkipByteOrderMark());
        statement.setEncoding(getEncoding());

        // JSON-specific options
        statement.setEnableOctal(getEnableOctal());
        statement.setAllowDuplicate(getAllowDuplicate());
        statement.setStripOuterArray(getStripOuterArray());
        statement.setStripNullValues(getStripNullValues());
        statement.setIgnoreUtf8Errors(getIgnoreUtf8Errors());

        // Parquet-specific options
        statement.setSnappyCompression(getSnappyCompression());
        statement.setBinaryAsText(getBinaryAsText());
        statement.setUseLogicalType(getUseLogicalType());
        statement.setUseVectorizedScanner(getUseVectorizedScanner());

        // XML-specific options
        statement.setPreserveSpace(getPreserveSpace());
        statement.setStripOuterElement(getStripOuterElement());
        statement.setDisableSnowflakeData(getDisableSnowflakeData());
        statement.setDisableAutoConvert(getDisableAutoConvert());

        return new SqlStatement[] { statement };
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.addAll(super.validate(database));

        if (getFileFormatName() == null || getFileFormatName().trim().isEmpty()) {
            validationErrors.addError("fileFormatName is required");
        }

        // Validate mutual exclusivity
        if (getOrReplace() != null && getOrReplace() && 
            getIfNotExists() != null && getIfNotExists()) {
            validationErrors.addError("Cannot specify both orReplace and ifNotExists");
        }

        if (getTemporary() != null && getTemporary() && 
            getVolatile() != null && getVolatile()) {
            validationErrors.addError("Cannot specify both temporary and volatile");
        }

        // Validate skipHeader is non-negative
        if (getSkipHeader() != null && getSkipHeader() < 0) {
            validationErrors.addError("skipHeader must be 0 or positive");
        }

        return validationErrors;
    }

    @Override
    public boolean supportsRollback(Database database) {
        return database instanceof SnowflakeDatabase;
    }
    
    @Override
    public Change[] createInverses() {
        DropFileFormatChange inverse = new DropFileFormatChange();
        inverse.setFileFormatName(getFileFormatName());
        inverse.setCatalogName(getCatalogName());
        inverse.setSchemaName(getSchemaName());
        inverse.setIfExists(true);
        return new Change[]{inverse};
    }

    @Override
    public String getConfirmationMessage() {
        return "File format " + getFileFormatName() + " created";
    }
}