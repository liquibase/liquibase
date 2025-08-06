package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.CreateFileFormatStatement;
import liquibase.structure.core.Table;
import liquibase.util.StringUtil;

public class CreateFileFormatGeneratorSnowflake extends AbstractSqlGenerator<CreateFileFormatStatement> {

    @Override
    public boolean supports(CreateFileFormatStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(CreateFileFormatStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = new ValidationErrors();
        
        // Required field validation
        if (statement.getFileFormatName() == null || statement.getFileFormatName().trim().isEmpty()) {
            errors.addError("File format name is required");
        }
        
        // Mutual exclusivity validation: OR REPLACE and IF NOT EXISTS cannot be used together
        if (Boolean.TRUE.equals(statement.getOrReplace()) && Boolean.TRUE.equals(statement.getIfNotExists())) {
            errors.addError("Cannot use both OR REPLACE and IF NOT EXISTS");
        }
        
        // Temporary and volatile are mutually exclusive
        if (Boolean.TRUE.equals(statement.getTemporary()) && Boolean.TRUE.equals(statement.getVolatile())) {
            errors.addError("Cannot use both TEMPORARY and VOLATILE");
        }
        
        // Skip header validation
        if (statement.getSkipHeader() != null && statement.getSkipHeader() < 0) {
            errors.addError("skipHeader must be 0 or positive");
        }
        
        // Validate escape and field delimiter are not the same (CSV only)
        if (statement.getEscape() != null && statement.getFieldDelimiter() != null &&
            statement.getEscape().equals(statement.getFieldDelimiter())) {
            errors.addError("Escape character cannot be the same as field delimiter");
        }
        
        // Format-specific validation
        String formatType = statement.getFileFormatType();
        if (formatType != null) {
            validateFormatSpecificOptions(statement, formatType, errors);
        }
        
        // Compression validation based on format type
        validateCompressionForFormat(statement, formatType, errors);
        
        return errors;
    }
    
    /**
     * Validates format-specific options based on the specified format type.
     */
    private void validateFormatSpecificOptions(CreateFileFormatStatement statement, String formatType, ValidationErrors errors) {
        formatType = formatType.toUpperCase();
        
        switch (formatType) {
            case "CSV":
                validateCsvFormatOptions(statement, errors);
                break;
            case "JSON":
                validateJsonFormatOptions(statement, errors);
                break;
            case "PARQUET":
                validateParquetFormatOptions(statement, errors);
                break;
            case "XML":
                validateXmlFormatOptions(statement, errors);
                break;
            case "AVRO":
                validateAvroFormatOptions(statement, errors);
                break;
            case "ORC":
                validateOrcFormatOptions(statement, errors);
                break;
            case "CUSTOM":
                // CUSTOM format allows most options
                break;
            default:
                errors.addError("Invalid file format type: " + formatType + ". Valid types are: CSV, JSON, AVRO, ORC, PARQUET, XML, CUSTOM");
        }
    }
    
    private void validateCsvFormatOptions(CreateFileFormatStatement statement, ValidationErrors errors) {
        // JSON-specific options should not be used with CSV
        if (statement.getEnableOctal() != null || statement.getAllowDuplicate() != null || 
            statement.getStripOuterArray() != null || statement.getStripNullValues() != null ||
            statement.getIgnoreUtf8Errors() != null) {
            errors.addError("JSON-specific options (enableOctal, allowDuplicate, stripOuterArray, stripNullValues, ignoreUtf8Errors) cannot be used with CSV format");
        }
        
        // PARQUET-specific options should not be used with CSV
        if (statement.getSnappyCompression() != null || statement.getBinaryAsText() != null || 
            statement.getUseLogicalType() != null || statement.getUseVectorizedScanner() != null) {
            errors.addError("PARQUET-specific options (snappyCompression, binaryAsText, useLogicalType, useVectorizedScanner) cannot be used with CSV format");
        }
        
        // XML-specific options should not be used with CSV
        if (statement.getPreserveSpace() != null || statement.getStripOuterElement() != null || 
            statement.getDisableSnowflakeData() != null || statement.getDisableAutoConvert() != null) {
            errors.addError("XML-specific options (preserveSpace, stripOuterElement, disableSnowflakeData, disableAutoConvert) cannot be used with CSV format");
        }
        
        // Validate CSV-specific encoding
        if (statement.getEncoding() != null) {
            String encoding = statement.getEncoding().toUpperCase();
            if (!encoding.equals("UTF8") && !encoding.equals("ISO-8859-1") && !encoding.equals("WINDOWS-1252")) {
                errors.addError("Invalid encoding for CSV: " + statement.getEncoding() + ". Valid encodings are: UTF8, ISO-8859-1, WINDOWS-1252");
            }
        }
    }
    
    private void validateJsonFormatOptions(CreateFileFormatStatement statement, ValidationErrors errors) {
        // CSV-specific options should not be used with JSON
        if (statement.getRecordDelimiter() != null || statement.getFieldDelimiter() != null || 
            statement.getParseHeader() != null || statement.getSkipHeader() != null || 
            statement.getSkipBlankLines() != null || statement.getEscape() != null || 
            statement.getEscapeUnenclosedField() != null || statement.getFieldOptionallyEnclosedBy() != null || 
            statement.getErrorOnColumnCountMismatch() != null || statement.getValidateUtf8() != null || 
            statement.getEmptyFieldAsNull() != null || statement.getEncoding() != null) {
            errors.addError("CSV-specific options cannot be used with JSON format");
        }
        
        // PARQUET-specific options should not be used with JSON
        if (statement.getSnappyCompression() != null || statement.getBinaryAsText() != null || 
            statement.getUseLogicalType() != null || statement.getUseVectorizedScanner() != null) {
            errors.addError("PARQUET-specific options cannot be used with JSON format");
        }
        
        // XML-specific options should not be used with JSON
        if (statement.getPreserveSpace() != null || statement.getStripOuterElement() != null || 
            statement.getDisableSnowflakeData() != null || statement.getDisableAutoConvert() != null) {
            errors.addError("XML-specific options cannot be used with JSON format");
        }
    }
    
    private void validateParquetFormatOptions(CreateFileFormatStatement statement, ValidationErrors errors) {
        // CSV-specific options should not be used with PARQUET
        if (statement.getRecordDelimiter() != null || statement.getFieldDelimiter() != null || 
            statement.getParseHeader() != null || statement.getSkipHeader() != null || 
            statement.getSkipBlankLines() != null || statement.getEscape() != null || 
            statement.getEscapeUnenclosedField() != null || statement.getFieldOptionallyEnclosedBy() != null || 
            statement.getErrorOnColumnCountMismatch() != null || statement.getValidateUtf8() != null || 
            statement.getEmptyFieldAsNull() != null || statement.getSkipByteOrderMark() != null || 
            statement.getEncoding() != null) {
            errors.addError("CSV-specific options cannot be used with PARQUET format");
        }
        
        // JSON-specific options should not be used with PARQUET
        if (statement.getEnableOctal() != null || statement.getAllowDuplicate() != null || 
            statement.getStripOuterArray() != null || statement.getStripNullValues() != null || 
            statement.getIgnoreUtf8Errors() != null) {
            errors.addError("JSON-specific options cannot be used with PARQUET format");
        }
        
        // XML-specific options should not be used with PARQUET
        if (statement.getPreserveSpace() != null || statement.getStripOuterElement() != null || 
            statement.getDisableSnowflakeData() != null || statement.getDisableAutoConvert() != null) {
            errors.addError("XML-specific options cannot be used with PARQUET format");
        }
    }
    
    private void validateXmlFormatOptions(CreateFileFormatStatement statement, ValidationErrors errors) {
        // CSV-specific options should not be used with XML
        if (statement.getRecordDelimiter() != null || statement.getFieldDelimiter() != null || 
            statement.getParseHeader() != null || statement.getSkipHeader() != null || 
            statement.getSkipBlankLines() != null || statement.getEscape() != null || 
            statement.getEscapeUnenclosedField() != null || statement.getFieldOptionallyEnclosedBy() != null || 
            statement.getErrorOnColumnCountMismatch() != null || statement.getValidateUtf8() != null || 
            statement.getEmptyFieldAsNull() != null || statement.getEncoding() != null) {
            errors.addError("CSV-specific options cannot be used with XML format");
        }
        
        // JSON-specific options should not be used with XML (except ignoreUtf8Errors which is shared)
        if (statement.getEnableOctal() != null || statement.getAllowDuplicate() != null || 
            statement.getStripOuterArray() != null || statement.getStripNullValues() != null) {
            errors.addError("JSON-specific options (enableOctal, allowDuplicate, stripOuterArray, stripNullValues) cannot be used with XML format");
        }
        
        // PARQUET-specific options should not be used with XML
        if (statement.getSnappyCompression() != null || statement.getBinaryAsText() != null || 
            statement.getUseLogicalType() != null || statement.getUseVectorizedScanner() != null) {
            errors.addError("PARQUET-specific options cannot be used with XML format");
        }
    }
    
    private void validateAvroFormatOptions(CreateFileFormatStatement statement, ValidationErrors errors) {
        // AVRO only supports: compression, trimSpace, replaceInvalidCharacters, nullIf
        if (statement.getRecordDelimiter() != null || statement.getFieldDelimiter() != null || 
            statement.getParseHeader() != null || statement.getSkipHeader() != null || 
            statement.getSkipBlankLines() != null) {
            errors.addError("CSV-specific options cannot be used with AVRO format");
        }
        
        if (statement.getEnableOctal() != null || statement.getAllowDuplicate() != null || 
            statement.getStripOuterArray() != null || statement.getStripNullValues() != null || 
            statement.getIgnoreUtf8Errors() != null) {
            errors.addError("JSON-specific options cannot be used with AVRO format");
        }
        
        if (statement.getSnappyCompression() != null || statement.getBinaryAsText() != null || 
            statement.getUseLogicalType() != null || statement.getUseVectorizedScanner() != null) {
            errors.addError("PARQUET-specific options cannot be used with AVRO format");
        }
        
        if (statement.getPreserveSpace() != null || statement.getStripOuterElement() != null || 
            statement.getDisableSnowflakeData() != null || statement.getDisableAutoConvert() != null) {
            errors.addError("XML-specific options cannot be used with AVRO format");
        }
    }
    
    private void validateOrcFormatOptions(CreateFileFormatStatement statement, ValidationErrors errors) {
        // ORC only supports: trimSpace, replaceInvalidCharacters, nullIf
        if (statement.getRecordDelimiter() != null || statement.getFieldDelimiter() != null || 
            statement.getParseHeader() != null || statement.getSkipHeader() != null) {
            errors.addError("CSV-specific options cannot be used with ORC format");
        }
        
        if (statement.getEnableOctal() != null || statement.getAllowDuplicate() != null || 
            statement.getStripOuterArray() != null || statement.getStripNullValues() != null) {
            errors.addError("JSON-specific options cannot be used with ORC format");
        }
        
        if (statement.getSnappyCompression() != null || statement.getBinaryAsText() != null || 
            statement.getUseLogicalType() != null || statement.getUseVectorizedScanner() != null) {
            errors.addError("PARQUET-specific options cannot be used with ORC format");
        }
        
        if (statement.getPreserveSpace() != null || statement.getStripOuterElement() != null || 
            statement.getDisableSnowflakeData() != null || statement.getDisableAutoConvert() != null) {
            errors.addError("XML-specific options cannot be used with ORC format");
        }
    }
    
    /**
     * Validates compression based on format type constraints.
     */
    private void validateCompressionForFormat(CreateFileFormatStatement statement, String formatType, ValidationErrors errors) {
        if (statement.getCompression() == null) return;
        
        String compression = statement.getCompression().toUpperCase();
        
        if ("PARQUET".equals(formatType)) {
            // PARQUET only supports: AUTO, LZO, SNAPPY, NONE
            if (!compression.equals("AUTO") && !compression.equals("LZO") && 
                !compression.equals("SNAPPY") && !compression.equals("NONE")) {
                errors.addError("Invalid compression for PARQUET: " + statement.getCompression() + ". Valid compressions are: AUTO, LZO, SNAPPY, NONE");
            }
        } else {
            // Other formats support full compression set
            if (!compression.equals("AUTO") && !compression.equals("GZIP") && 
                !compression.equals("BZ2") && !compression.equals("BROTLI") && 
                !compression.equals("ZSTD") && !compression.equals("DEFLATE") && 
                !compression.equals("RAW_DEFLATE") && !compression.equals("NONE")) {
                errors.addError("Invalid compression: " + statement.getCompression() + ". Valid compressions are: AUTO, GZIP, BZ2, BROTLI, ZSTD, DEFLATE, RAW_DEFLATE, NONE");
            }
        }
    }

    @Override
    public Sql[] generateSql(CreateFileFormatStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE ");
        
        // Add OR REPLACE if specified
        if (Boolean.TRUE.equals(statement.getOrReplace())) {
            sql.append("OR REPLACE ");
        }
        
        // Add temporary/volatile modifiers
        if (Boolean.TRUE.equals(statement.getTemporary())) {
            sql.append("TEMPORARY ");
        } else if (Boolean.TRUE.equals(statement.getVolatile())) {
            sql.append("VOLATILE ");
        }
        
        sql.append("FILE FORMAT ");
        
        // Add IF NOT EXISTS if specified
        if (Boolean.TRUE.equals(statement.getIfNotExists())) {
            sql.append("IF NOT EXISTS ");
        }
        
        // Add file format name with proper schema qualification
        String qualifiedName = buildQualifiedName(statement, database);
        sql.append(qualifiedName);
        
        // Build format options
        StringBuilder optionsClause = new StringBuilder();
        boolean hasOptions = false;
        
        // Add format type first if specified
        if (statement.getFileFormatType() != null) {
            optionsClause.append(" TYPE = ").append(statement.getFileFormatType());
            hasOptions = true;
        }
        
        // Add format-specific options based on type
        hasOptions = addFormatOptions(statement, optionsClause, hasOptions);
        
        // Add common options
        hasOptions = addCommonOptions(statement, optionsClause, hasOptions);
        
        // Add comment last if specified
        if (statement.getComment() != null) {
            if (hasOptions) optionsClause.append(" ");
            optionsClause.append("COMMENT = '").append(escapeString(statement.getComment())).append("'");
            hasOptions = true;
        }
        
        if (hasOptions) {
            sql.append(optionsClause);
        }
        
        return new Sql[] { new UnparsedSql(sql.toString()) };
    }
    
    private String buildQualifiedName(CreateFileFormatStatement statement, Database database) {
        StringBuilder qualifiedName = new StringBuilder();
        
        if (statement.getCatalogName() != null) {
            qualifiedName.append(database.escapeObjectName(statement.getCatalogName(), Table.class))
                        .append(".");
        }
        
        if (statement.getSchemaName() != null) {
            qualifiedName.append(database.escapeObjectName(statement.getSchemaName(), Table.class))
                        .append(".");
        }
        
        qualifiedName.append(database.escapeObjectName(statement.getFileFormatName(), Table.class));
        
        return qualifiedName.toString();
    }
    
    private boolean addFormatOptions(CreateFileFormatStatement statement, StringBuilder optionsClause, boolean hasOptions) {
        String formatType = statement.getFileFormatType();
        
        if ("CSV".equals(formatType)) {
            hasOptions = addCsvOptions(statement, optionsClause, hasOptions);
        } else if ("JSON".equals(formatType)) {
            hasOptions = addJsonOptions(statement, optionsClause, hasOptions);
        } else if ("PARQUET".equals(formatType)) {
            hasOptions = addParquetOptions(statement, optionsClause, hasOptions);
        } else if ("XML".equals(formatType)) {
            hasOptions = addXmlOptions(statement, optionsClause, hasOptions);
        }
        
        return hasOptions;
    }
    
    private boolean addCsvOptions(CreateFileFormatStatement statement, StringBuilder optionsClause, boolean hasOptions) {
        hasOptions = addOption(optionsClause, hasOptions, "RECORD_DELIMITER", statement.getRecordDelimiter(), true);
        hasOptions = addOption(optionsClause, hasOptions, "FIELD_DELIMITER", statement.getFieldDelimiter(), true);
        hasOptions = addOption(optionsClause, hasOptions, "PARSE_HEADER", statement.getParseHeader(), false);
        hasOptions = addOption(optionsClause, hasOptions, "SKIP_HEADER", statement.getSkipHeader(), false);
        hasOptions = addOption(optionsClause, hasOptions, "SKIP_BLANK_LINES", statement.getSkipBlankLines(), false);
        hasOptions = addOption(optionsClause, hasOptions, "ESCAPE", statement.getEscape(), true);
        hasOptions = addOption(optionsClause, hasOptions, "ESCAPE_UNENCLOSED_FIELD", statement.getEscapeUnenclosedField(), true);
        hasOptions = addOption(optionsClause, hasOptions, "FIELD_OPTIONALLY_ENCLOSED_BY", statement.getFieldOptionallyEnclosedBy(), true);
        hasOptions = addOption(optionsClause, hasOptions, "ERROR_ON_COLUMN_COUNT_MISMATCH", statement.getErrorOnColumnCountMismatch(), false);
        hasOptions = addOption(optionsClause, hasOptions, "VALIDATE_UTF8", statement.getValidateUtf8(), false);
        hasOptions = addOption(optionsClause, hasOptions, "EMPTY_FIELD_AS_NULL", statement.getEmptyFieldAsNull(), false);
        hasOptions = addOption(optionsClause, hasOptions, "SKIP_BYTE_ORDER_MARK", statement.getSkipByteOrderMark(), false);
        hasOptions = addOption(optionsClause, hasOptions, "ENCODING", statement.getEncoding(), true);
        
        return hasOptions;
    }
    
    private boolean addJsonOptions(CreateFileFormatStatement statement, StringBuilder optionsClause, boolean hasOptions) {
        hasOptions = addOption(optionsClause, hasOptions, "ENABLE_OCTAL", statement.getEnableOctal(), false);
        hasOptions = addOption(optionsClause, hasOptions, "ALLOW_DUPLICATE", statement.getAllowDuplicate(), false);
        hasOptions = addOption(optionsClause, hasOptions, "STRIP_OUTER_ARRAY", statement.getStripOuterArray(), false);
        hasOptions = addOption(optionsClause, hasOptions, "STRIP_NULL_VALUES", statement.getStripNullValues(), false);
        hasOptions = addOption(optionsClause, hasOptions, "IGNORE_UTF8_ERRORS", statement.getIgnoreUtf8Errors(), false);
        
        return hasOptions;
    }
    
    private boolean addParquetOptions(CreateFileFormatStatement statement, StringBuilder optionsClause, boolean hasOptions) {
        hasOptions = addOption(optionsClause, hasOptions, "SNAPPY_COMPRESSION", statement.getSnappyCompression(), false);
        hasOptions = addOption(optionsClause, hasOptions, "BINARY_AS_TEXT", statement.getBinaryAsText(), false);
        hasOptions = addOption(optionsClause, hasOptions, "USE_LOGICAL_TYPE", statement.getUseLogicalType(), false);
        hasOptions = addOption(optionsClause, hasOptions, "USE_VECTORIZED_SCANNER", statement.getUseVectorizedScanner(), false);
        
        return hasOptions;
    }
    
    private boolean addXmlOptions(CreateFileFormatStatement statement, StringBuilder optionsClause, boolean hasOptions) {
        hasOptions = addOption(optionsClause, hasOptions, "PRESERVE_SPACE", statement.getPreserveSpace(), false);
        hasOptions = addOption(optionsClause, hasOptions, "STRIP_OUTER_ELEMENT", statement.getStripOuterElement(), false);
        hasOptions = addOption(optionsClause, hasOptions, "DISABLE_SNOWFLAKE_DATA", statement.getDisableSnowflakeData(), false);
        hasOptions = addOption(optionsClause, hasOptions, "DISABLE_AUTO_CONVERT", statement.getDisableAutoConvert(), false);
        hasOptions = addOption(optionsClause, hasOptions, "IGNORE_UTF8_ERRORS", statement.getIgnoreUtf8Errors(), false);
        
        return hasOptions;
    }
    
    private boolean addCommonOptions(CreateFileFormatStatement statement, StringBuilder optionsClause, boolean hasOptions) {
        hasOptions = addOption(optionsClause, hasOptions, "COMPRESSION", statement.getCompression(), false);
        hasOptions = addOption(optionsClause, hasOptions, "DATE_FORMAT", statement.getDateFormat(), true);
        hasOptions = addOption(optionsClause, hasOptions, "TIME_FORMAT", statement.getTimeFormat(), true);
        hasOptions = addOption(optionsClause, hasOptions, "TIMESTAMP_FORMAT", statement.getTimestampFormat(), true);
        hasOptions = addOption(optionsClause, hasOptions, "BINARY_FORMAT", statement.getBinaryFormat(), false);
        hasOptions = addOption(optionsClause, hasOptions, "TRIM_SPACE", statement.getTrimSpace(), false);
        hasOptions = addOption(optionsClause, hasOptions, "REPLACE_INVALID_CHARACTERS", statement.getReplaceInvalidCharacters(), false);
        hasOptions = addOption(optionsClause, hasOptions, "FILE_EXTENSION", statement.getFileExtension(), true);
        
        // Handle NULL_IF specially as it can be a list
        if (statement.getNullIf() != null) {
            if (hasOptions) optionsClause.append(" ");
            optionsClause.append("NULL_IF = (");
            
            // Split by comma and quote each value
            String[] nullValues = statement.getNullIf().split(",");
            for (int i = 0; i < nullValues.length; i++) {
                if (i > 0) optionsClause.append(", ");
                optionsClause.append("'").append(escapeString(nullValues[i].trim())).append("'");
            }
            
            optionsClause.append(")");
            hasOptions = true;
        }
        
        return hasOptions;
    }
    
    private boolean addOption(StringBuilder optionsClause, boolean hasOptions, String optionName, Object value, boolean quoted) {
        if (value != null) {
            if (hasOptions) optionsClause.append(" ");
            optionsClause.append(optionName).append(" = ");
            
            if (quoted && value instanceof String) {
                optionsClause.append("'").append(escapeString((String) value)).append("'");
            } else {
                optionsClause.append(value);
            }
            
            return true;
        }
        return hasOptions;
    }
    
    private String escapeString(String value) {
        if (value == null) return null;
        return value.replace("'", "''");
    }
}