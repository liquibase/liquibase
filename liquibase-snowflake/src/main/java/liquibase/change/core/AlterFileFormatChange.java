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
import liquibase.statement.core.AlterFileFormatStatement;

/**
 * Alters an existing file format in Snowflake.
 */
@DatabaseChange(
    name = "alterFileFormat",
    description = "Alters a file format",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "fileFormat",
    since = "4.33"
)
public class AlterFileFormatChange extends AbstractChange {

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
    
    // All format options for SET operations (inherits from CREATE)
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
    @DatabaseChangeProperty(description = "Name of the file format to alter", requiredForDatabase = "snowflake")
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

    @DatabaseChangeProperty(description = "Use IF EXISTS clause")
    public Boolean getIfExists() {
        return ifExists;
    }

    public void setIfExists(Boolean ifExists) {
        this.ifExists = ifExists;
    }

    @DatabaseChangeProperty(description = "New name for rename operation")
    public String getNewFileFormatName() {
        return newFileFormatName;
    }

    public void setNewFileFormatName(String newFileFormatName) {
        this.newFileFormatName = newFileFormatName;
    }

    @DatabaseChangeProperty(description = "Type of alter operation (SET, RENAME, UNSET)")
    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    // SET Operations Getters/Setters
    @DatabaseChangeProperty(description = "New format type")
    public String getNewFileFormatType() {
        return newFileFormatType;
    }

    public void setNewFileFormatType(String newFileFormatType) {
        this.newFileFormatType = newFileFormatType;
    }

    @DatabaseChangeProperty(description = "New comment")
    public String getNewComment() {
        return newComment;
    }

    public void setNewComment(String newComment) {
        this.newComment = newComment;
    }

    // Format Options Getters/Setters (same as CREATE for brevity, showing key ones)
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

    // UNSET Operations Getters/Setters
    @DatabaseChangeProperty(description = "Remove comment")
    public Boolean getUnsetComment() {
        return unsetComment;
    }

    public void setUnsetComment(Boolean unsetComment) {
        this.unsetComment = unsetComment;
    }

    @DatabaseChangeProperty(description = "Reset compression to default")
    public Boolean getUnsetCompression() {
        return unsetCompression;
    }

    public void setUnsetCompression(Boolean unsetCompression) {
        this.unsetCompression = unsetCompression;
    }

    // Additional format option getters/setters
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

    @DatabaseChangeProperty(description = "Binary format")
    public String getBinaryFormat() {
        return binaryFormat;
    }

    public void setBinaryFormat(String binaryFormat) {
        this.binaryFormat = binaryFormat;
    }

    @DatabaseChangeProperty(description = "Trim space")
    public Boolean getTrimSpace() {
        return trimSpace;
    }

    public void setTrimSpace(Boolean trimSpace) {
        this.trimSpace = trimSpace;
    }

    @DatabaseChangeProperty(description = "Field delimiter")
    public String getFieldDelimiter() {
        return fieldDelimiter;
    }

    public void setFieldDelimiter(String fieldDelimiter) {
        this.fieldDelimiter = fieldDelimiter;
    }

    @DatabaseChangeProperty(description = "Skip header lines")
    public Integer getSkipHeader() {
        return skipHeader;
    }

    public void setSkipHeader(Integer skipHeader) {
        this.skipHeader = skipHeader;
    }

    @DatabaseChangeProperty(description = "NULL_IF values")
    public String getNullIf() {
        return nullIf;
    }

    public void setNullIf(String nullIf) {
        this.nullIf = nullIf;
    }

    @DatabaseChangeProperty(description = "Replace invalid characters")
    public Boolean getReplaceInvalidCharacters() {
        return replaceInvalidCharacters;
    }

    public void setReplaceInvalidCharacters(Boolean replaceInvalidCharacters) {
        this.replaceInvalidCharacters = replaceInvalidCharacters;
    }

    @DatabaseChangeProperty(description = "File extension")
    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    // Additional UNSET getters/setters
    @DatabaseChangeProperty(description = "Unset date format")
    public Boolean getUnsetDateFormat() {
        return unsetDateFormat;
    }

    public void setUnsetDateFormat(Boolean unsetDateFormat) {
        this.unsetDateFormat = unsetDateFormat;
    }

    @DatabaseChangeProperty(description = "Unset time format")
    public Boolean getUnsetTimeFormat() {
        return unsetTimeFormat;
    }

    public void setUnsetTimeFormat(Boolean unsetTimeFormat) {
        this.unsetTimeFormat = unsetTimeFormat;
    }

    @DatabaseChangeProperty(description = "Unset timestamp format")
    public Boolean getUnsetTimestampFormat() {
        return unsetTimestampFormat;
    }

    public void setUnsetTimestampFormat(Boolean unsetTimestampFormat) {
        this.unsetTimestampFormat = unsetTimestampFormat;
    }

    @DatabaseChangeProperty(description = "Unset binary format")
    public Boolean getUnsetBinaryFormat() {
        return unsetBinaryFormat;
    }

    public void setUnsetBinaryFormat(Boolean unsetBinaryFormat) {
        this.unsetBinaryFormat = unsetBinaryFormat;
    }

    @DatabaseChangeProperty(description = "Unset trim space")
    public Boolean getUnsetTrimSpace() {
        return unsetTrimSpace;
    }

    public void setUnsetTrimSpace(Boolean unsetTrimSpace) {
        this.unsetTrimSpace = unsetTrimSpace;
    }

    @DatabaseChangeProperty(description = "Unset NULL_IF")
    public Boolean getUnsetNullIf() {
        return unsetNullIf;
    }

    public void setUnsetNullIf(Boolean unsetNullIf) {
        this.unsetNullIf = unsetNullIf;
    }

    @DatabaseChangeProperty(description = "Unset file extension")
    public Boolean getUnsetFileExtension() {
        return unsetFileExtension;
    }

    public void setUnsetFileExtension(Boolean unsetFileExtension) {
        this.unsetFileExtension = unsetFileExtension;
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        
        // Core properties
        statement.setFileFormatName(getFileFormatName());
        statement.setCatalogName(getCatalogName());
        statement.setSchemaName(getSchemaName());
        statement.setIfExists(getIfExists());
        statement.setNewFileFormatName(getNewFileFormatName());
        statement.setOperationType(getOperationType());
        
        // SET operations
        statement.setNewFileFormatType(getNewFileFormatType());
        statement.setNewComment(getNewComment());
        
        // Format options for SET
        statement.setCompression(getCompression());
        statement.setDateFormat(getDateFormat());
        statement.setTimeFormat(getTimeFormat());
        statement.setTimestampFormat(getTimestampFormat());
        statement.setBinaryFormat(getBinaryFormat());
        statement.setTrimSpace(getTrimSpace());
        statement.setFieldDelimiter(getFieldDelimiter());
        statement.setSkipHeader(getSkipHeader());
        statement.setNullIf(getNullIf());
        statement.setReplaceInvalidCharacters(getReplaceInvalidCharacters());
        statement.setFileExtension(getFileExtension());
        
        // UNSET operations
        statement.setUnsetComment(getUnsetComment());
        statement.setUnsetCompression(getUnsetCompression());
        statement.setUnsetDateFormat(getUnsetDateFormat());
        statement.setUnsetTimeFormat(getUnsetTimeFormat());
        statement.setUnsetTimestampFormat(getUnsetTimestampFormat());
        statement.setUnsetBinaryFormat(getUnsetBinaryFormat());
        statement.setUnsetTrimSpace(getUnsetTrimSpace());
        statement.setUnsetNullIf(getUnsetNullIf());
        statement.setUnsetFileExtension(getUnsetFileExtension());

        return new SqlStatement[] { statement };
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.addAll(super.validate(database));

        if (getFileFormatName() == null || getFileFormatName().trim().isEmpty()) {
            validationErrors.addError("fileFormatName is required");
        }

        // Validate operation type
        String opType = getOperationType();
        if (opType != null && !opType.matches("SET|RENAME|UNSET")) {
            validationErrors.addError("operationType must be SET, RENAME, or UNSET");
        }
        
        // RENAME requires newFileFormatName
        if ("RENAME".equals(opType) && (getNewFileFormatName() == null || getNewFileFormatName().trim().isEmpty())) {
            validationErrors.addError("newFileFormatName is required for RENAME operation");
        }

        return validationErrors;
    }

    @Override
    public boolean supportsRollback(Database database) {
        return database instanceof SnowflakeDatabase && "RENAME".equals(getOperationType());
    }
    
    @Override
    public Change[] createInverses() {
        if ("RENAME".equals(getOperationType()) && getNewFileFormatName() != null) {
            AlterFileFormatChange inverse = new AlterFileFormatChange();
            inverse.setFileFormatName(getNewFileFormatName());
            inverse.setCatalogName(getCatalogName());
            inverse.setSchemaName(getSchemaName());
            inverse.setNewFileFormatName(getFileFormatName());
            inverse.setOperationType("RENAME");
            inverse.setIfExists(true);
            return new Change[]{inverse};
        }
        return new Change[0];
    }

    @Override
    public String getConfirmationMessage() {
        return "File format " + getFileFormatName() + " altered";
    }
}