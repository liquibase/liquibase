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
    
    // UNSET Operations - Complete set based on requirements
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
    private Boolean unsetValidateUtf8;
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

    @DatabaseChangeProperty(description = "Unset replace invalid characters")
    public Boolean getUnsetReplaceInvalidCharacters() {
        return unsetReplaceInvalidCharacters;
    }

    public void setUnsetReplaceInvalidCharacters(Boolean unsetReplaceInvalidCharacters) {
        this.unsetReplaceInvalidCharacters = unsetReplaceInvalidCharacters;
    }

    // CSV-specific UNSET getters/setters
    @DatabaseChangeProperty(description = "Unset record delimiter")
    public Boolean getUnsetRecordDelimiter() {
        return unsetRecordDelimiter;
    }

    public void setUnsetRecordDelimiter(Boolean unsetRecordDelimiter) {
        this.unsetRecordDelimiter = unsetRecordDelimiter;
    }

    @DatabaseChangeProperty(description = "Unset field delimiter")
    public Boolean getUnsetFieldDelimiter() {
        return unsetFieldDelimiter;
    }

    public void setUnsetFieldDelimiter(Boolean unsetFieldDelimiter) {
        this.unsetFieldDelimiter = unsetFieldDelimiter;
    }

    @DatabaseChangeProperty(description = "Unset parse header")
    public Boolean getUnsetParseHeader() {
        return unsetParseHeader;
    }

    public void setUnsetParseHeader(Boolean unsetParseHeader) {
        this.unsetParseHeader = unsetParseHeader;
    }

    @DatabaseChangeProperty(description = "Unset skip header")
    public Boolean getUnsetSkipHeader() {
        return unsetSkipHeader;
    }

    public void setUnsetSkipHeader(Boolean unsetSkipHeader) {
        this.unsetSkipHeader = unsetSkipHeader;
    }

    @DatabaseChangeProperty(description = "Unset skip blank lines")
    public Boolean getUnsetSkipBlankLines() {
        return unsetSkipBlankLines;
    }

    public void setUnsetSkipBlankLines(Boolean unsetSkipBlankLines) {
        this.unsetSkipBlankLines = unsetSkipBlankLines;
    }

    @DatabaseChangeProperty(description = "Unset escape character")
    public Boolean getUnsetEscape() {
        return unsetEscape;
    }

    public void setUnsetEscape(Boolean unsetEscape) {
        this.unsetEscape = unsetEscape;
    }

    @DatabaseChangeProperty(description = "Unset escape unenclosed field")
    public Boolean getUnsetEscapeUnenclosedField() {
        return unsetEscapeUnenclosedField;
    }

    public void setUnsetEscapeUnenclosedField(Boolean unsetEscapeUnenclosedField) {
        this.unsetEscapeUnenclosedField = unsetEscapeUnenclosedField;
    }

    @DatabaseChangeProperty(description = "Unset field optionally enclosed by")
    public Boolean getUnsetFieldOptionallyEnclosedBy() {
        return unsetFieldOptionallyEnclosedBy;
    }

    public void setUnsetFieldOptionallyEnclosedBy(Boolean unsetFieldOptionallyEnclosedBy) {
        this.unsetFieldOptionallyEnclosedBy = unsetFieldOptionallyEnclosedBy;
    }

    @DatabaseChangeProperty(description = "Unset error on column count mismatch")
    public Boolean getUnsetErrorOnColumnCountMismatch() {
        return unsetErrorOnColumnCountMismatch;
    }

    public void setUnsetErrorOnColumnCountMismatch(Boolean unsetErrorOnColumnCountMismatch) {
        this.unsetErrorOnColumnCountMismatch = unsetErrorOnColumnCountMismatch;
    }

    @DatabaseChangeProperty(description = "Unset validate UTF8")
    public Boolean getUnsetValidateUtf8() {
        return unsetValidateUtf8;
    }

    public void setUnsetValidateUtf8(Boolean unsetValidateUtf8) {
        this.unsetValidateUtf8 = unsetValidateUtf8;
    }

    @DatabaseChangeProperty(description = "Unset empty field as null")
    public Boolean getUnsetEmptyFieldAsNull() {
        return unsetEmptyFieldAsNull;
    }

    public void setUnsetEmptyFieldAsNull(Boolean unsetEmptyFieldAsNull) {
        this.unsetEmptyFieldAsNull = unsetEmptyFieldAsNull;
    }

    @DatabaseChangeProperty(description = "Unset skip byte order mark")
    public Boolean getUnsetSkipByteOrderMark() {
        return unsetSkipByteOrderMark;
    }

    public void setUnsetSkipByteOrderMark(Boolean unsetSkipByteOrderMark) {
        this.unsetSkipByteOrderMark = unsetSkipByteOrderMark;
    }

    @DatabaseChangeProperty(description = "Unset encoding")
    public Boolean getUnsetEncoding() {
        return unsetEncoding;
    }

    public void setUnsetEncoding(Boolean unsetEncoding) {
        this.unsetEncoding = unsetEncoding;
    }

    // JSON-specific UNSET getters/setters
    @DatabaseChangeProperty(description = "Unset enable octal")
    public Boolean getUnsetEnableOctal() {
        return unsetEnableOctal;
    }

    public void setUnsetEnableOctal(Boolean unsetEnableOctal) {
        this.unsetEnableOctal = unsetEnableOctal;
    }

    @DatabaseChangeProperty(description = "Unset allow duplicate")
    public Boolean getUnsetAllowDuplicate() {
        return unsetAllowDuplicate;
    }

    public void setUnsetAllowDuplicate(Boolean unsetAllowDuplicate) {
        this.unsetAllowDuplicate = unsetAllowDuplicate;
    }

    @DatabaseChangeProperty(description = "Unset strip outer array")
    public Boolean getUnsetStripOuterArray() {
        return unsetStripOuterArray;
    }

    public void setUnsetStripOuterArray(Boolean unsetStripOuterArray) {
        this.unsetStripOuterArray = unsetStripOuterArray;
    }

    @DatabaseChangeProperty(description = "Unset strip null values")
    public Boolean getUnsetStripNullValues() {
        return unsetStripNullValues;
    }

    public void setUnsetStripNullValues(Boolean unsetStripNullValues) {
        this.unsetStripNullValues = unsetStripNullValues;
    }

    @DatabaseChangeProperty(description = "Unset ignore UTF8 errors")
    public Boolean getUnsetIgnoreUtf8Errors() {
        return unsetIgnoreUtf8Errors;
    }

    public void setUnsetIgnoreUtf8Errors(Boolean unsetIgnoreUtf8Errors) {
        this.unsetIgnoreUtf8Errors = unsetIgnoreUtf8Errors;
    }

    // PARQUET-specific UNSET getters/setters
    @DatabaseChangeProperty(description = "Unset snappy compression")
    public Boolean getUnsetSnappyCompression() {
        return unsetSnappyCompression;
    }

    public void setUnsetSnappyCompression(Boolean unsetSnappyCompression) {
        this.unsetSnappyCompression = unsetSnappyCompression;
    }

    @DatabaseChangeProperty(description = "Unset binary as text")
    public Boolean getUnsetBinaryAsText() {
        return unsetBinaryAsText;
    }

    public void setUnsetBinaryAsText(Boolean unsetBinaryAsText) {
        this.unsetBinaryAsText = unsetBinaryAsText;
    }

    @DatabaseChangeProperty(description = "Unset use logical type")
    public Boolean getUnsetUseLogicalType() {
        return unsetUseLogicalType;
    }

    public void setUnsetUseLogicalType(Boolean unsetUseLogicalType) {
        this.unsetUseLogicalType = unsetUseLogicalType;
    }

    @DatabaseChangeProperty(description = "Unset use vectorized scanner")
    public Boolean getUnsetUseVectorizedScanner() {
        return unsetUseVectorizedScanner;
    }

    public void setUnsetUseVectorizedScanner(Boolean unsetUseVectorizedScanner) {
        this.unsetUseVectorizedScanner = unsetUseVectorizedScanner;
    }

    // XML-specific UNSET getters/setters
    @DatabaseChangeProperty(description = "Unset preserve space")
    public Boolean getUnsetPreserveSpace() {
        return unsetPreserveSpace;
    }

    public void setUnsetPreserveSpace(Boolean unsetPreserveSpace) {
        this.unsetPreserveSpace = unsetPreserveSpace;
    }

    @DatabaseChangeProperty(description = "Unset strip outer element")
    public Boolean getUnsetStripOuterElement() {
        return unsetStripOuterElement;
    }

    public void setUnsetStripOuterElement(Boolean unsetStripOuterElement) {
        this.unsetStripOuterElement = unsetStripOuterElement;
    }

    @DatabaseChangeProperty(description = "Unset disable Snowflake data")
    public Boolean getUnsetDisableSnowflakeData() {
        return unsetDisableSnowflakeData;
    }

    public void setUnsetDisableSnowflakeData(Boolean unsetDisableSnowflakeData) {
        this.unsetDisableSnowflakeData = unsetDisableSnowflakeData;
    }

    @DatabaseChangeProperty(description = "Unset disable auto convert")
    public Boolean getUnsetDisableAutoConvert() {
        return unsetDisableAutoConvert;
    }

    public void setUnsetDisableAutoConvert(Boolean unsetDisableAutoConvert) {
        this.unsetDisableAutoConvert = unsetDisableAutoConvert;
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
        
        // UNSET operations - Complete set
        statement.setUnsetComment(getUnsetComment());
        statement.setUnsetCompression(getUnsetCompression());
        statement.setUnsetDateFormat(getUnsetDateFormat());
        statement.setUnsetTimeFormat(getUnsetTimeFormat());
        statement.setUnsetTimestampFormat(getUnsetTimestampFormat());
        statement.setUnsetBinaryFormat(getUnsetBinaryFormat());
        statement.setUnsetTrimSpace(getUnsetTrimSpace());
        statement.setUnsetNullIf(getUnsetNullIf());
        statement.setUnsetFileExtension(getUnsetFileExtension());
        statement.setUnsetReplaceInvalidCharacters(getUnsetReplaceInvalidCharacters());
        
        // CSV-specific UNSET operations
        statement.setUnsetRecordDelimiter(getUnsetRecordDelimiter());
        statement.setUnsetFieldDelimiter(getUnsetFieldDelimiter());
        statement.setUnsetParseHeader(getUnsetParseHeader());
        statement.setUnsetSkipHeader(getUnsetSkipHeader());
        statement.setUnsetSkipBlankLines(getUnsetSkipBlankLines());
        statement.setUnsetEscape(getUnsetEscape());
        statement.setUnsetEscapeUnenclosedField(getUnsetEscapeUnenclosedField());
        statement.setUnsetFieldOptionallyEnclosedBy(getUnsetFieldOptionallyEnclosedBy());
        statement.setUnsetErrorOnColumnCountMismatch(getUnsetErrorOnColumnCountMismatch());
        // statement.setUnsetValidateUtf8 removed - not available in Snowflake INFORMATION_SCHEMA.FILE_FORMATS
        statement.setUnsetEmptyFieldAsNull(getUnsetEmptyFieldAsNull());
        statement.setUnsetSkipByteOrderMark(getUnsetSkipByteOrderMark());
        statement.setUnsetEncoding(getUnsetEncoding());
        
        // JSON-specific UNSET operations
        statement.setUnsetEnableOctal(getUnsetEnableOctal());
        statement.setUnsetAllowDuplicate(getUnsetAllowDuplicate());
        statement.setUnsetStripOuterArray(getUnsetStripOuterArray());
        statement.setUnsetStripNullValues(getUnsetStripNullValues());
        statement.setUnsetIgnoreUtf8Errors(getUnsetIgnoreUtf8Errors());
        
        // PARQUET-specific UNSET operations
        statement.setUnsetSnappyCompression(getUnsetSnappyCompression());
        statement.setUnsetBinaryAsText(getUnsetBinaryAsText());
        statement.setUnsetUseLogicalType(getUnsetUseLogicalType());
        statement.setUnsetUseVectorizedScanner(getUnsetUseVectorizedScanner());
        
        // XML-specific UNSET operations
        statement.setUnsetPreserveSpace(getUnsetPreserveSpace());
        statement.setUnsetStripOuterElement(getUnsetStripOuterElement());
        statement.setUnsetDisableSnowflakeData(getUnsetDisableSnowflakeData());
        statement.setUnsetDisableAutoConvert(getUnsetDisableAutoConvert());

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
        
        // Validate mutual exclusivity of operations
        validateOperationMutualExclusivity(validationErrors);
        
        // RENAME requires newFileFormatName
        if ("RENAME".equals(opType) && (getNewFileFormatName() == null || getNewFileFormatName().trim().isEmpty())) {
            validationErrors.addError("newFileFormatName is required for RENAME operation");
        }
        
        // Validate SET operation constraints
        if ("SET".equals(opType)) {
            validateSetOperations(validationErrors);
        }
        
        // Validate UNSET operation constraints
        if ("UNSET".equals(opType)) {
            validateUnsetOperations(validationErrors);
        }

        return validationErrors;
    }

    /**
     * Validates that SET, RENAME, and UNSET operations are mutually exclusive.
     */
    private void validateOperationMutualExclusivity(ValidationErrors validationErrors) {
        boolean hasSetOperations = hasSetOperations();
        boolean hasRenameOperation = getNewFileFormatName() != null;
        boolean hasUnsetOperations = hasUnsetOperations();
        
        int operationCount = 0;
        if (hasSetOperations) operationCount++;
        if (hasRenameOperation) operationCount++;
        if (hasUnsetOperations) operationCount++;
        
        if (operationCount > 1) {
            validationErrors.addError("SET, RENAME, and UNSET operations are mutually exclusive. Only one operation type is allowed per ALTER statement.");
        }
        
        if (operationCount == 0) {
            validationErrors.addError("At least one operation is required: SET properties, RENAME to new name, or UNSET properties.");
        }
    }
    
    /**
     * Validates SET operation constraints.
     */
    private void validateSetOperations(ValidationErrors validationErrors) {
        if (!hasSetOperations()) {
            validationErrors.addError("SET operation specified but no properties to set are provided");
        }
        
        // Validate format-specific constraints for SET operations
        String newFormatType = getNewFileFormatType();
        if (newFormatType != null) {
            validateFormatSpecificSetOptions(newFormatType, validationErrors);
        }
        
        // Validate compression constraints
        if (getCompression() != null) {
            validateCompressionForSetOperation(newFormatType, validationErrors);
        }
    }
    
    /**
     * Validates UNSET operation constraints.
     */
    private void validateUnsetOperations(ValidationErrors validationErrors) {
        if (!hasUnsetOperations()) {
            validationErrors.addError("UNSET operation specified but no properties to unset are provided");
        }
    }
    
    /**
     * Checks if any SET operations are defined.
     */
    private boolean hasSetOperations() {
        return getNewFileFormatType() != null ||
               getNewComment() != null ||
               getCompression() != null ||
               getDateFormat() != null ||
               getTimeFormat() != null ||
               getTimestampFormat() != null ||
               getBinaryFormat() != null ||
               getTrimSpace() != null ||
               getNullIf() != null ||
               getReplaceInvalidCharacters() != null ||
               getFileExtension() != null ||
               getFieldDelimiter() != null ||
               getSkipHeader() != null;
    }
    
    /**
     * Checks if any UNSET operations are defined.
     */
    private boolean hasUnsetOperations() {
        return Boolean.TRUE.equals(getUnsetComment()) ||
               Boolean.TRUE.equals(getUnsetCompression()) ||
               Boolean.TRUE.equals(getUnsetDateFormat()) ||
               Boolean.TRUE.equals(getUnsetTimeFormat()) ||
               Boolean.TRUE.equals(getUnsetTimestampFormat()) ||
               Boolean.TRUE.equals(getUnsetBinaryFormat()) ||
               Boolean.TRUE.equals(getUnsetTrimSpace()) ||
               Boolean.TRUE.equals(getUnsetNullIf()) ||
               Boolean.TRUE.equals(getUnsetFileExtension()) ||
               Boolean.TRUE.equals(getUnsetReplaceInvalidCharacters()) ||
               // CSV-specific UNSET operations
               Boolean.TRUE.equals(getUnsetRecordDelimiter()) ||
               Boolean.TRUE.equals(getUnsetFieldDelimiter()) ||
               Boolean.TRUE.equals(getUnsetParseHeader()) ||
               Boolean.TRUE.equals(getUnsetSkipHeader()) ||
               Boolean.TRUE.equals(getUnsetSkipBlankLines()) ||
               Boolean.TRUE.equals(getUnsetEscape()) ||
               Boolean.TRUE.equals(getUnsetEscapeUnenclosedField()) ||
               Boolean.TRUE.equals(getUnsetFieldOptionallyEnclosedBy()) ||
               Boolean.TRUE.equals(getUnsetErrorOnColumnCountMismatch()) ||
               Boolean.TRUE.equals(getUnsetValidateUtf8()) ||
               Boolean.TRUE.equals(getUnsetEmptyFieldAsNull()) ||
               Boolean.TRUE.equals(getUnsetSkipByteOrderMark()) ||
               Boolean.TRUE.equals(getUnsetEncoding()) ||
               // JSON-specific UNSET operations
               Boolean.TRUE.equals(getUnsetEnableOctal()) ||
               Boolean.TRUE.equals(getUnsetAllowDuplicate()) ||
               Boolean.TRUE.equals(getUnsetStripOuterArray()) ||
               Boolean.TRUE.equals(getUnsetStripNullValues()) ||
               Boolean.TRUE.equals(getUnsetIgnoreUtf8Errors()) ||
               // PARQUET-specific UNSET operations
               Boolean.TRUE.equals(getUnsetSnappyCompression()) ||
               Boolean.TRUE.equals(getUnsetBinaryAsText()) ||
               Boolean.TRUE.equals(getUnsetUseLogicalType()) ||
               Boolean.TRUE.equals(getUnsetUseVectorizedScanner()) ||
               // XML-specific UNSET operations
               Boolean.TRUE.equals(getUnsetPreserveSpace()) ||
               Boolean.TRUE.equals(getUnsetStripOuterElement()) ||
               Boolean.TRUE.equals(getUnsetDisableSnowflakeData()) ||
               Boolean.TRUE.equals(getUnsetDisableAutoConvert());
    }
    
    /**
     * Validates format-specific options for SET operations.
     */
    private void validateFormatSpecificSetOptions(String formatType, ValidationErrors validationErrors) {
        if (formatType == null) return;
        
        formatType = formatType.toUpperCase();
        
        switch (formatType) {
            case "CSV":
                validateCsvSetOptions(validationErrors);
                break;
            case "JSON":
                validateJsonSetOptions(validationErrors);
                break;
            case "PARQUET":
                validateParquetSetOptions(validationErrors);
                break;
            case "XML":
                validateXmlSetOptions(validationErrors);
                break;
            case "AVRO":
                validateAvroSetOptions(validationErrors);
                break;
            case "ORC":
                validateOrcSetOptions(validationErrors);
                break;
            case "CUSTOM":
                // CUSTOM format allows most options
                break;
            default:
                validationErrors.addError("Invalid file format type: " + formatType + ". Valid types are: CSV, JSON, AVRO, ORC, PARQUET, XML, CUSTOM");
        }
    }
    
    private void validateCsvSetOptions(ValidationErrors validationErrors) {
        // Only CSV-specific and common options are allowed
        // Format-specific options from other formats should not be set
        if (hasNonCsvFormatOptions()) {
            validationErrors.addError("Non-CSV format options cannot be set when format type is CSV");
        }
    }
    
    private void validateJsonSetOptions(ValidationErrors validationErrors) {
        // Only JSON-specific and common options are allowed
        if (hasNonJsonFormatOptions()) {
            validationErrors.addError("Non-JSON format options cannot be set when format type is JSON");
        }
    }
    
    private void validateParquetSetOptions(ValidationErrors validationErrors) {
        // Only PARQUET-specific and common options are allowed
        if (hasNonParquetFormatOptions()) {
            validationErrors.addError("Non-PARQUET format options cannot be set when format type is PARQUET");
        }
    }
    
    private void validateXmlSetOptions(ValidationErrors validationErrors) {
        // Only XML-specific and common options are allowed
        if (hasNonXmlFormatOptions()) {
            validationErrors.addError("Non-XML format options cannot be set when format type is XML");
        }
    }
    
    private void validateAvroSetOptions(ValidationErrors validationErrors) {
        // AVRO only supports common options, no format-specific options
        if (hasAnyFormatSpecificOptions()) {
            validationErrors.addError("Format-specific options cannot be set when format type is AVRO");
        }
    }
    
    private void validateOrcSetOptions(ValidationErrors validationErrors) {
        // ORC only supports common options, no format-specific options
        if (hasAnyFormatSpecificOptions()) {
            validationErrors.addError("Format-specific options cannot be set when format type is ORC");
        }
    }
    
    /**
     * Validates compression based on the new format type.
     */
    private void validateCompressionForSetOperation(String formatType, ValidationErrors validationErrors) {
        if (getCompression() == null) return;
        
        String compression = getCompression().toUpperCase();
        
        if ("PARQUET".equals(formatType)) {
            // PARQUET only supports: AUTO, LZO, SNAPPY, NONE
            if (!compression.equals("AUTO") && !compression.equals("LZO") && 
                !compression.equals("SNAPPY") && !compression.equals("NONE")) {
                validationErrors.addError("Invalid compression for PARQUET: " + getCompression() + ". Valid compressions are: AUTO, LZO, SNAPPY, NONE");
            }
        } else {
            // Other formats support full compression set
            if (!compression.equals("AUTO") && !compression.equals("GZIP") && 
                !compression.equals("BZ2") && !compression.equals("BROTLI") && 
                !compression.equals("ZSTD") && !compression.equals("DEFLATE") && 
                !compression.equals("RAW_DEFLATE") && !compression.equals("NONE")) {
                validationErrors.addError("Invalid compression: " + getCompression() + ". Valid compressions are: AUTO, GZIP, BZ2, BROTLI, ZSTD, DEFLATE, RAW_DEFLATE, NONE");
            }
        }
    }
    
    private boolean hasNonCsvFormatOptions() {
        // Check for JSON, PARQUET, XML specific UNSET options (SET options not implemented for format-specific properties)
        return 
            // JSON-specific UNSET options
            Boolean.TRUE.equals(getUnsetEnableOctal()) || Boolean.TRUE.equals(getUnsetAllowDuplicate()) || 
            Boolean.TRUE.equals(getUnsetStripOuterArray()) || Boolean.TRUE.equals(getUnsetStripNullValues()) || 
            Boolean.TRUE.equals(getUnsetIgnoreUtf8Errors()) ||
            // PARQUET-specific UNSET options
            Boolean.TRUE.equals(getUnsetSnappyCompression()) || Boolean.TRUE.equals(getUnsetBinaryAsText()) || 
            Boolean.TRUE.equals(getUnsetUseLogicalType()) || Boolean.TRUE.equals(getUnsetUseVectorizedScanner()) ||
            // XML-specific UNSET options
            Boolean.TRUE.equals(getUnsetPreserveSpace()) || Boolean.TRUE.equals(getUnsetStripOuterElement()) || 
            Boolean.TRUE.equals(getUnsetDisableSnowflakeData()) || Boolean.TRUE.equals(getUnsetDisableAutoConvert());
    }
    
    private boolean hasNonJsonFormatOptions() {
        // Check for CSV, PARQUET, XML specific options (available SET properties for CSV, UNSET for others)
        return 
            // CSV-specific SET/UNSET options (limited SET properties available)
            getFieldDelimiter() != null || getSkipHeader() != null || 
            Boolean.TRUE.equals(getUnsetRecordDelimiter()) || Boolean.TRUE.equals(getUnsetFieldDelimiter()) ||
            Boolean.TRUE.equals(getUnsetParseHeader()) || Boolean.TRUE.equals(getUnsetSkipHeader()) || 
            Boolean.TRUE.equals(getUnsetSkipBlankLines()) || Boolean.TRUE.equals(getUnsetEscape()) ||
            Boolean.TRUE.equals(getUnsetEscapeUnenclosedField()) || Boolean.TRUE.equals(getUnsetFieldOptionallyEnclosedBy()) || 
            Boolean.TRUE.equals(getUnsetErrorOnColumnCountMismatch()) || Boolean.TRUE.equals(getUnsetEmptyFieldAsNull()) || 
            Boolean.TRUE.equals(getUnsetSkipByteOrderMark()) || Boolean.TRUE.equals(getUnsetEncoding()) ||
            // PARQUET-specific UNSET options
            Boolean.TRUE.equals(getUnsetSnappyCompression()) || Boolean.TRUE.equals(getUnsetBinaryAsText()) || 
            Boolean.TRUE.equals(getUnsetUseLogicalType()) || Boolean.TRUE.equals(getUnsetUseVectorizedScanner()) ||
            // XML-specific UNSET options
            Boolean.TRUE.equals(getUnsetPreserveSpace()) || Boolean.TRUE.equals(getUnsetStripOuterElement()) || 
            Boolean.TRUE.equals(getUnsetDisableSnowflakeData()) || Boolean.TRUE.equals(getUnsetDisableAutoConvert());
    }
    
    private boolean hasNonParquetFormatOptions() {
        // Check for CSV, JSON, XML specific options (available SET properties for CSV, UNSET for others)
        return 
            // CSV-specific SET/UNSET options (limited SET properties available)
            getFieldDelimiter() != null || getSkipHeader() != null || 
            Boolean.TRUE.equals(getUnsetRecordDelimiter()) || Boolean.TRUE.equals(getUnsetFieldDelimiter()) ||
            Boolean.TRUE.equals(getUnsetParseHeader()) || Boolean.TRUE.equals(getUnsetSkipHeader()) || 
            Boolean.TRUE.equals(getUnsetSkipBlankLines()) || Boolean.TRUE.equals(getUnsetEscape()) ||
            Boolean.TRUE.equals(getUnsetEscapeUnenclosedField()) || Boolean.TRUE.equals(getUnsetFieldOptionallyEnclosedBy()) || 
            Boolean.TRUE.equals(getUnsetErrorOnColumnCountMismatch()) || Boolean.TRUE.equals(getUnsetEmptyFieldAsNull()) || 
            Boolean.TRUE.equals(getUnsetSkipByteOrderMark()) || Boolean.TRUE.equals(getUnsetEncoding()) ||
            // JSON-specific UNSET options
            Boolean.TRUE.equals(getUnsetEnableOctal()) || Boolean.TRUE.equals(getUnsetAllowDuplicate()) || 
            Boolean.TRUE.equals(getUnsetStripOuterArray()) || Boolean.TRUE.equals(getUnsetStripNullValues()) || 
            Boolean.TRUE.equals(getUnsetIgnoreUtf8Errors()) ||
            // XML-specific UNSET options
            Boolean.TRUE.equals(getUnsetPreserveSpace()) || Boolean.TRUE.equals(getUnsetStripOuterElement()) || 
            Boolean.TRUE.equals(getUnsetDisableSnowflakeData()) || Boolean.TRUE.equals(getUnsetDisableAutoConvert());
    }
    
    private boolean hasNonXmlFormatOptions() {
        // Check for CSV, JSON, PARQUET specific options (available SET properties for CSV, UNSET for others)
        return 
            // CSV-specific SET/UNSET options (limited SET properties available)
            getFieldDelimiter() != null || getSkipHeader() != null || 
            Boolean.TRUE.equals(getUnsetRecordDelimiter()) || Boolean.TRUE.equals(getUnsetFieldDelimiter()) ||
            Boolean.TRUE.equals(getUnsetParseHeader()) || Boolean.TRUE.equals(getUnsetSkipHeader()) || 
            Boolean.TRUE.equals(getUnsetSkipBlankLines()) || Boolean.TRUE.equals(getUnsetEscape()) ||
            Boolean.TRUE.equals(getUnsetEscapeUnenclosedField()) || Boolean.TRUE.equals(getUnsetFieldOptionallyEnclosedBy()) || 
            Boolean.TRUE.equals(getUnsetErrorOnColumnCountMismatch()) || Boolean.TRUE.equals(getUnsetEmptyFieldAsNull()) || 
            Boolean.TRUE.equals(getUnsetSkipByteOrderMark()) || Boolean.TRUE.equals(getUnsetEncoding()) ||
            // JSON-specific UNSET options
            Boolean.TRUE.equals(getUnsetEnableOctal()) || Boolean.TRUE.equals(getUnsetAllowDuplicate()) || 
            Boolean.TRUE.equals(getUnsetStripOuterArray()) || Boolean.TRUE.equals(getUnsetStripNullValues()) || 
            Boolean.TRUE.equals(getUnsetIgnoreUtf8Errors()) ||
            // PARQUET-specific UNSET options
            Boolean.TRUE.equals(getUnsetSnappyCompression()) || Boolean.TRUE.equals(getUnsetBinaryAsText()) || 
            Boolean.TRUE.equals(getUnsetUseLogicalType()) || Boolean.TRUE.equals(getUnsetUseVectorizedScanner());
    }
    
    private boolean hasAnyFormatSpecificOptions() {
        // Check for any format-specific options (available SET properties for CSV, UNSET for all)
        return 
            // CSV-specific SET/UNSET options (limited SET properties available)
            getFieldDelimiter() != null || getSkipHeader() != null || 
            Boolean.TRUE.equals(getUnsetRecordDelimiter()) || Boolean.TRUE.equals(getUnsetFieldDelimiter()) ||
            Boolean.TRUE.equals(getUnsetParseHeader()) || Boolean.TRUE.equals(getUnsetSkipHeader()) || 
            Boolean.TRUE.equals(getUnsetSkipBlankLines()) || Boolean.TRUE.equals(getUnsetEscape()) ||
            Boolean.TRUE.equals(getUnsetEscapeUnenclosedField()) || Boolean.TRUE.equals(getUnsetFieldOptionallyEnclosedBy()) || 
            Boolean.TRUE.equals(getUnsetErrorOnColumnCountMismatch()) || Boolean.TRUE.equals(getUnsetEmptyFieldAsNull()) || 
            Boolean.TRUE.equals(getUnsetSkipByteOrderMark()) || Boolean.TRUE.equals(getUnsetEncoding()) ||
            // JSON-specific UNSET options
            Boolean.TRUE.equals(getUnsetEnableOctal()) || Boolean.TRUE.equals(getUnsetAllowDuplicate()) || 
            Boolean.TRUE.equals(getUnsetStripOuterArray()) || Boolean.TRUE.equals(getUnsetStripNullValues()) || 
            Boolean.TRUE.equals(getUnsetIgnoreUtf8Errors()) ||
            // PARQUET-specific UNSET options
            Boolean.TRUE.equals(getUnsetSnappyCompression()) || Boolean.TRUE.equals(getUnsetBinaryAsText()) || 
            Boolean.TRUE.equals(getUnsetUseLogicalType()) || Boolean.TRUE.equals(getUnsetUseVectorizedScanner()) ||
            // XML-specific UNSET options
            Boolean.TRUE.equals(getUnsetPreserveSpace()) || Boolean.TRUE.equals(getUnsetStripOuterElement()) || 
            Boolean.TRUE.equals(getUnsetDisableSnowflakeData()) || Boolean.TRUE.equals(getUnsetDisableAutoConvert());
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