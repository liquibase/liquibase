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

    /**
     * Enhanced validation method that ensures the statement configuration is valid
     * based on Snowflake CREATE FILE FORMAT constraints and format-specific options.
     */
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        // Basic validation
        if (fileFormatName == null || fileFormatName.trim().isEmpty()) {
            result.addError("File format name is required");
        } else if (fileFormatName.length() > 255 || !fileFormatName.matches("^[A-Za-z_][A-Za-z0-9_]*$")) {
            result.addError("Invalid file format name. Must start with letter/underscore, contain only letters/numbers/underscores, max 255 characters: " + fileFormatName);
        }
        
        // Validate OR REPLACE vs IF NOT EXISTS mutual exclusivity
        if (Boolean.TRUE.equals(orReplace) && Boolean.TRUE.equals(ifNotExists)) {
            result.addError("OR REPLACE and IF NOT EXISTS cannot be used together");
        }
        
        // Validate TEMPORARY vs VOLATILE mutual exclusivity
        if (Boolean.TRUE.equals(temporary) && Boolean.TRUE.equals(_volatile)) {
            result.addError("TEMPORARY and VOLATILE cannot be used together");
        }
        
        // Validate file format type enumeration
        if (fileFormatType != null) {
            String[] validTypes = {"CSV", "JSON", "PARQUET", "ORC", "AVRO", "XML", "CUSTOM"};
            boolean validType = false;
            for (String validType2 : validTypes) {
                if (validType2.equalsIgnoreCase(fileFormatType)) {
                    validType = true;
                    break;
                }
            }
            if (!validType) {
                result.addError("Invalid file format type '" + fileFormatType + "'. Valid types: CSV, JSON, PARQUET, ORC, AVRO, XML, CUSTOM");
            }
        }
        
        // Validate compression enumeration (format-specific)
        if (compression != null) {
            validateCompressionForFormat(result);
        }
        
        // Validate encoding enumeration
        if (encoding != null) {
            String[] validEncodings = {"UTF8", "ISO88591", "WINDOWS1252", "UTF16", "UTF16LE", "UTF16BE", "UTF32", "UTF32LE", "UTF32BE"};
            boolean validEncoding = false;
            for (String validEnc : validEncodings) {
                if (validEnc.equalsIgnoreCase(encoding)) {
                    validEncoding = true;
                    break;
                }
            }
            if (!validEncoding) {
                result.addError("Invalid encoding '" + encoding + "'. Valid values: UTF8, ISO88591, WINDOWS1252, UTF16, UTF16LE, UTF16BE, UTF32, UTF32LE, UTF32BE");
            }
        }
        
        // Validate skipHeader range
        if (skipHeader != null && skipHeader < 0) {
            result.addError("SKIP_HEADER must be >= 0, got: " + skipHeader);
        }
        
        // Format-specific validation
        if (fileFormatType != null) {
            validateFormatSpecificOptions(result);
        }
        
        // Validate character delimiter conflicts
        validateCharacterDelimiterConflicts(result);
        
        return result;
    }
    
    private void validateFormatSpecificOptions(ValidationResult result) {
        String formatType = fileFormatType.toUpperCase();
        
        switch (formatType) {
            case "CSV":
                validateCsvOptions(result);
                break;
            case "JSON":
                validateJsonOptions(result);
                break;
            case "PARQUET":
            case "ORC":
            case "AVRO":
                validateBinaryFormatOptions(result);
                break;
            case "XML":
                validateXmlOptions(result);
                break;
            case "CUSTOM":
                // CUSTOM format allows any combination of options - no specific validation needed
                break;
        }
    }
    
    private void validateCsvOptions(ValidationResult result) {
        // CSV-specific options validation
        if (enableOctal != null || allowDuplicate != null || stripOuterArray != null) {
            result.addWarning("JSON-specific options (ENABLE_OCTAL, ALLOW_DUPLICATE, STRIP_OUTER_ARRAY) are ignored for CSV format");
        }
        
        // Validate field/record delimiter conflicts
        if (fieldDelimiter != null && recordDelimiter != null && fieldDelimiter.equals(recordDelimiter)) {
            result.addError("FIELD_DELIMITER and RECORD_DELIMITER cannot be the same character");
        }
    }
    
    private void validateJsonOptions(ValidationResult result) {
        // JSON-specific options validation
        if (parseHeader != null || skipHeader != null || fieldDelimiter != null) {
            result.addWarning("CSV-specific options (PARSE_HEADER, SKIP_HEADER, FIELD_DELIMITER) are ignored for JSON format");
        }
    }
    
    private void validateBinaryFormatOptions(ValidationResult result) {
        // Binary formats (PARQUET, ORC, AVRO) have limited options
        if (fieldDelimiter != null || recordDelimiter != null || escape != null) {
            result.addWarning("Delimiter and escape options are ignored for binary formats (PARQUET, ORC, AVRO)");
        }
    }
    
    private void validateXmlOptions(ValidationResult result) {
        // XML-specific validation
        if (fieldDelimiter != null || recordDelimiter != null) {
            result.addWarning("CSV delimiter options are ignored for XML format");
        }
    }
    
    private void validateCompressionForFormat(ValidationResult result) {
        if (fileFormatType == null) {
            // Use general compression validation if no format specified
            String[] validCompressions = {"AUTO", "GZIP", "BZ2", "BROTLI", "ZSTD", "DEFLATE", "RAW_DEFLATE", "NONE"};
            boolean validCompression = false;
            for (String validComp : validCompressions) {
                if (validComp.equalsIgnoreCase(compression)) {
                    validCompression = true;
                    break;
                }
            }
            if (!validCompression) {
                result.addError("Invalid compression '" + compression + "'. Valid values: AUTO, GZIP, BZ2, BROTLI, ZSTD, DEFLATE, RAW_DEFLATE, NONE");
            }
            return;
        }
        
        String formatType = fileFormatType.toUpperCase();
        String[] validCompressions;
        
        switch (formatType) {
            case "CSV":
            case "JSON":
            case "XML":
                validCompressions = new String[]{"AUTO", "GZIP", "BZ2", "BROTLI", "ZSTD", "DEFLATE", "RAW_DEFLATE", "NONE"};
                break;
            case "PARQUET":
                validCompressions = new String[]{"AUTO", "SNAPPY", "GZIP", "LZO", "NONE"};
                break;
            case "ORC":
                validCompressions = new String[]{"AUTO", "SNAPPY", "GZIP", "ZLIB", "NONE"};
                break;
            case "AVRO":
                validCompressions = new String[]{"AUTO", "DEFLATE", "SNAPPY", "NONE"};
                break;
            case "CUSTOM":
                // CUSTOM format allows any compression
                return;
            default:
                // Fallback to general validation
                validCompressions = new String[]{"AUTO", "GZIP", "BZ2", "BROTLI", "ZSTD", "DEFLATE", "RAW_DEFLATE", "NONE"};
                break;
        }
        
        boolean validCompression = false;
        for (String validComp : validCompressions) {
            if (validComp.equalsIgnoreCase(compression)) {
                validCompression = true;
                break;
            }
        }
        if (!validCompression) {
            result.addError("Invalid compression '" + compression + "' for " + formatType + " format. Valid values: " + 
                String.join(", ", validCompressions));
        }
    }
    
    private void validateCharacterDelimiterConflicts(ValidationResult result) {
        // Check for escape character conflicts
        if (escape != null && fieldDelimiter != null && escape.equals(fieldDelimiter)) {
            result.addError("ESCAPE character cannot be the same as FIELD_DELIMITER");
        }
        
        if (escape != null && fieldOptionallyEnclosedBy != null && escape.equals(fieldOptionallyEnclosedBy)) {
            result.addError("ESCAPE character cannot be the same as FIELD_OPTIONALLY_ENCLOSED_BY");
        }
        
        // Check for single-character constraints
        if (fieldDelimiter != null && fieldDelimiter.length() > 1) {
            result.addError("FIELD_DELIMITER must be a single character, got: '" + fieldDelimiter + "'");
        }
        
        if (escape != null && escape.length() > 1) {
            result.addError("ESCAPE must be a single character, got: '" + escape + "'");
        }
        
        if (fieldOptionallyEnclosedBy != null && fieldOptionallyEnclosedBy.length() > 1) {
            result.addError("FIELD_OPTIONALLY_ENCLOSED_BY must be a single character, got: '" + fieldOptionallyEnclosedBy + "'");
        }
    }
    
    /**
     * Simple validation result container
     */
    public static class ValidationResult {
        private final java.util.List<String> errors = new java.util.ArrayList<>();
        private final java.util.List<String> warnings = new java.util.ArrayList<>();
        
        public boolean hasErrors() { return !errors.isEmpty(); }
        public boolean hasWarnings() { return !warnings.isEmpty(); }
        public java.util.List<String> getErrors() { return errors; }
        public java.util.List<String> getWarnings() { return warnings; }
        
        public void addError(String error) { errors.add(error); }
        public void addWarning(String warning) { warnings.add(warning); }
    }
}