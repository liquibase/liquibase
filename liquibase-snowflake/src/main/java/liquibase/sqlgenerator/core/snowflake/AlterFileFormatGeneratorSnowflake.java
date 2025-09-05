package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.AlterFileFormatStatement;
import liquibase.structure.core.Table;

public class AlterFileFormatGeneratorSnowflake extends AbstractSqlGenerator<AlterFileFormatStatement> {

    @Override
    public boolean supports(AlterFileFormatStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(AlterFileFormatStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = new ValidationErrors();
        
        // Required field validation
        if (statement.getFileFormatName() == null || statement.getFileFormatName().trim().isEmpty()) {
            errors.addError("File format name is required");
        }
        
        // Validate operation type
        String opType = statement.getOperationType();
        if (opType != null && !opType.matches("SET|RENAME|UNSET")) {
            errors.addError("operationType must be SET, RENAME, or UNSET");
        }
        
        // RENAME requires newFileFormatName
        if ("RENAME".equals(opType) && (statement.getNewFileFormatName() == null || statement.getNewFileFormatName().trim().isEmpty())) {
            errors.addError("newFileFormatName is required for RENAME operation");
        }
        
        return errors;
    }

    @Override
    public Sql[] generateSql(AlterFileFormatStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sql = new StringBuilder();
        sql.append("ALTER FILE FORMAT ");
        
        // Add IF EXISTS if specified
        if (Boolean.TRUE.equals(statement.getIfExists())) {
            sql.append("IF EXISTS ");
        }
        
        // Add file format name with proper schema qualification
        String qualifiedName = buildQualifiedName(statement, database);
        sql.append(qualifiedName);
        
        // Generate based on operation type
        String operationType = statement.getOperationType();
        if ("RENAME".equals(operationType)) {
            sql.append(" RENAME TO ").append(database.escapeObjectName(statement.getNewFileFormatName(), Table.class));
        } else if ("SET".equals(operationType)) {
            sql.append(" SET");
            addSetOptions(statement, sql);
        } else if ("UNSET".equals(operationType)) {
            sql.append(" UNSET");
            addUnsetOptions(statement, sql);
        }
        
        return new Sql[] { new UnparsedSql(sql.toString()) };
    }
    
    private String buildQualifiedName(AlterFileFormatStatement statement, Database database) {
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
    
    private void addSetOptions(AlterFileFormatStatement statement, StringBuilder sql) {
        boolean hasOptions = false;
        
        // Add new format type first if specified
        if (statement.getNewFileFormatType() != null) {
            sql.append(" TYPE = ").append(statement.getNewFileFormatType());
            hasOptions = true;
            
            // If format type is changing, automatically unset format-specific options
            hasOptions = addFormatTypeChangeUnsetOptions(statement, sql, hasOptions);
        }
        
        // Add common format options
        hasOptions = addOption(sql, hasOptions, "COMPRESSION", statement.getCompression(), false);
        hasOptions = addOption(sql, hasOptions, "DATE_FORMAT", statement.getDateFormat(), true);
        hasOptions = addOption(sql, hasOptions, "TIME_FORMAT", statement.getTimeFormat(), true);
        hasOptions = addOption(sql, hasOptions, "TIMESTAMP_FORMAT", statement.getTimestampFormat(), true);
        hasOptions = addOption(sql, hasOptions, "BINARY_FORMAT", statement.getBinaryFormat(), false);
        hasOptions = addOption(sql, hasOptions, "TRIM_SPACE", statement.getTrimSpace(), false);
        hasOptions = addOption(sql, hasOptions, "REPLACE_INVALID_CHARACTERS", statement.getReplaceInvalidCharacters(), false);
        hasOptions = addOption(sql, hasOptions, "FILE_EXTENSION", statement.getFileExtension(), true);
        
        // Add CSV-specific options
        hasOptions = addOption(sql, hasOptions, "RECORD_DELIMITER", statement.getRecordDelimiter(), true);
        hasOptions = addOption(sql, hasOptions, "FIELD_DELIMITER", statement.getFieldDelimiter(), true);
        hasOptions = addOption(sql, hasOptions, "PARSE_HEADER", statement.getParseHeader(), false);
        hasOptions = addOption(sql, hasOptions, "SKIP_HEADER", statement.getSkipHeader(), false);
        hasOptions = addOption(sql, hasOptions, "SKIP_BLANK_LINES", statement.getSkipBlankLines(), false);
        hasOptions = addOption(sql, hasOptions, "ESCAPE", statement.getEscape(), true);
        hasOptions = addOption(sql, hasOptions, "ESCAPE_UNENCLOSED_FIELD", statement.getEscapeUnenclosedField(), true);
        hasOptions = addOption(sql, hasOptions, "FIELD_OPTIONALLY_ENCLOSED_BY", statement.getFieldOptionallyEnclosedBy(), true);
        hasOptions = addOption(sql, hasOptions, "ERROR_ON_COLUMN_COUNT_MISMATCH", statement.getErrorOnColumnCountMismatch(), false);
        // VALIDATE_UTF8 removed - not available in Snowflake INFORMATION_SCHEMA.FILE_FORMATS
        hasOptions = addOption(sql, hasOptions, "EMPTY_FIELD_AS_NULL", statement.getEmptyFieldAsNull(), false);
        hasOptions = addOption(sql, hasOptions, "SKIP_BYTE_ORDER_MARK", statement.getSkipByteOrderMark(), false);
        hasOptions = addOption(sql, hasOptions, "ENCODING", statement.getEncoding(), true);
        
        // Add JSON-specific options
        hasOptions = addOption(sql, hasOptions, "ENABLE_OCTAL", statement.getEnableOctal(), false);
        hasOptions = addOption(sql, hasOptions, "ALLOW_DUPLICATE", statement.getAllowDuplicate(), false);
        hasOptions = addOption(sql, hasOptions, "STRIP_OUTER_ARRAY", statement.getStripOuterArray(), false);
        hasOptions = addOption(sql, hasOptions, "STRIP_NULL_VALUES", statement.getStripNullValues(), false);
        hasOptions = addOption(sql, hasOptions, "IGNORE_UTF8_ERRORS", statement.getIgnoreUtf8Errors(), false);
        
        // Add PARQUET-specific options
        hasOptions = addOption(sql, hasOptions, "SNAPPY_COMPRESSION", statement.getSnappyCompression(), false);
        hasOptions = addOption(sql, hasOptions, "BINARY_AS_TEXT", statement.getBinaryAsText(), false);
        hasOptions = addOption(sql, hasOptions, "USE_LOGICAL_TYPE", statement.getUseLogicalType(), false);
        hasOptions = addOption(sql, hasOptions, "USE_VECTORIZED_SCANNER", statement.getUseVectorizedScanner(), false);
        
        // Add XML-specific options
        hasOptions = addOption(sql, hasOptions, "PRESERVE_SPACE", statement.getPreserveSpace(), false);
        hasOptions = addOption(sql, hasOptions, "STRIP_OUTER_ELEMENT", statement.getStripOuterElement(), false);
        hasOptions = addOption(sql, hasOptions, "DISABLE_SNOWFLAKE_DATA", statement.getDisableSnowflakeData(), false);
        hasOptions = addOption(sql, hasOptions, "DISABLE_AUTO_CONVERT", statement.getDisableAutoConvert(), false);
        
        // Handle NULL_IF specially
        if (statement.getNullIf() != null) {
            if (hasOptions) sql.append(", ");
            else sql.append(" "); // Always add space after SET
            sql.append("NULL_IF = (");
            
            String[] nullValues = statement.getNullIf().split(",");
            for (int i = 0; i < nullValues.length; i++) {
                if (i > 0) sql.append(", ");
                sql.append("'").append(escapeString(nullValues[i].trim())).append("'");
            }
            
            sql.append(")");
            hasOptions = true;
        }
        
        // Add comment last if specified
        if (statement.getNewComment() != null) {
            if (hasOptions) sql.append(", ");
            else sql.append(" "); // Always add space after SET
            sql.append("COMMENT = '").append(escapeString(statement.getNewComment())).append("'");
        }
    }
    
    private void addUnsetOptions(AlterFileFormatStatement statement, StringBuilder sql) {
        boolean hasOptions = false;
        
        // Common UNSET options
        hasOptions = addUnsetOption(sql, hasOptions, "COMMENT", statement.getUnsetComment());
        hasOptions = addUnsetOption(sql, hasOptions, "COMPRESSION", statement.getUnsetCompression());
        hasOptions = addUnsetOption(sql, hasOptions, "DATE_FORMAT", statement.getUnsetDateFormat());
        hasOptions = addUnsetOption(sql, hasOptions, "TIME_FORMAT", statement.getUnsetTimeFormat());
        hasOptions = addUnsetOption(sql, hasOptions, "TIMESTAMP_FORMAT", statement.getUnsetTimestampFormat());
        hasOptions = addUnsetOption(sql, hasOptions, "BINARY_FORMAT", statement.getUnsetBinaryFormat());
        hasOptions = addUnsetOption(sql, hasOptions, "TRIM_SPACE", statement.getUnsetTrimSpace());
        hasOptions = addUnsetOption(sql, hasOptions, "NULL_IF", statement.getUnsetNullIf());
        hasOptions = addUnsetOption(sql, hasOptions, "FILE_EXTENSION", statement.getUnsetFileExtension());
        hasOptions = addUnsetOption(sql, hasOptions, "REPLACE_INVALID_CHARACTERS", statement.getUnsetReplaceInvalidCharacters());
        
        // CSV-specific UNSET options
        hasOptions = addUnsetOption(sql, hasOptions, "RECORD_DELIMITER", statement.getUnsetRecordDelimiter());
        hasOptions = addUnsetOption(sql, hasOptions, "FIELD_DELIMITER", statement.getUnsetFieldDelimiter());
        hasOptions = addUnsetOption(sql, hasOptions, "PARSE_HEADER", statement.getUnsetParseHeader());
        hasOptions = addUnsetOption(sql, hasOptions, "SKIP_HEADER", statement.getUnsetSkipHeader());
        hasOptions = addUnsetOption(sql, hasOptions, "SKIP_BLANK_LINES", statement.getUnsetSkipBlankLines());
        hasOptions = addUnsetOption(sql, hasOptions, "ESCAPE", statement.getUnsetEscape());
        hasOptions = addUnsetOption(sql, hasOptions, "ESCAPE_UNENCLOSED_FIELD", statement.getUnsetEscapeUnenclosedField());
        hasOptions = addUnsetOption(sql, hasOptions, "FIELD_OPTIONALLY_ENCLOSED_BY", statement.getUnsetFieldOptionallyEnclosedBy());
        hasOptions = addUnsetOption(sql, hasOptions, "ERROR_ON_COLUMN_COUNT_MISMATCH", statement.getUnsetErrorOnColumnCountMismatch());
        // VALIDATE_UTF8 UNSET removed - not available in Snowflake INFORMATION_SCHEMA.FILE_FORMATS
        hasOptions = addUnsetOption(sql, hasOptions, "EMPTY_FIELD_AS_NULL", statement.getUnsetEmptyFieldAsNull());
        hasOptions = addUnsetOption(sql, hasOptions, "SKIP_BYTE_ORDER_MARK", statement.getUnsetSkipByteOrderMark());
        hasOptions = addUnsetOption(sql, hasOptions, "ENCODING", statement.getUnsetEncoding());
        
        // JSON-specific UNSET options
        hasOptions = addUnsetOption(sql, hasOptions, "ENABLE_OCTAL", statement.getUnsetEnableOctal());
        hasOptions = addUnsetOption(sql, hasOptions, "ALLOW_DUPLICATE", statement.getUnsetAllowDuplicate());
        hasOptions = addUnsetOption(sql, hasOptions, "STRIP_OUTER_ARRAY", statement.getUnsetStripOuterArray());
        hasOptions = addUnsetOption(sql, hasOptions, "STRIP_NULL_VALUES", statement.getUnsetStripNullValues());
        hasOptions = addUnsetOption(sql, hasOptions, "IGNORE_UTF8_ERRORS", statement.getUnsetIgnoreUtf8Errors());
        
        // PARQUET-specific UNSET options
        hasOptions = addUnsetOption(sql, hasOptions, "SNAPPY_COMPRESSION", statement.getUnsetSnappyCompression());
        hasOptions = addUnsetOption(sql, hasOptions, "BINARY_AS_TEXT", statement.getUnsetBinaryAsText());
        hasOptions = addUnsetOption(sql, hasOptions, "USE_LOGICAL_TYPE", statement.getUnsetUseLogicalType());
        hasOptions = addUnsetOption(sql, hasOptions, "USE_VECTORIZED_SCANNER", statement.getUnsetUseVectorizedScanner());
        
        // XML-specific UNSET options
        hasOptions = addUnsetOption(sql, hasOptions, "PRESERVE_SPACE", statement.getUnsetPreserveSpace());
        hasOptions = addUnsetOption(sql, hasOptions, "STRIP_OUTER_ELEMENT", statement.getUnsetStripOuterElement());
        hasOptions = addUnsetOption(sql, hasOptions, "DISABLE_SNOWFLAKE_DATA", statement.getUnsetDisableSnowflakeData());
        hasOptions = addUnsetOption(sql, hasOptions, "DISABLE_AUTO_CONVERT", statement.getUnsetDisableAutoConvert());
    }
    
    private boolean addUnsetOption(StringBuilder sql, boolean hasOptions, String optionName, Boolean unsetFlag) {
        if (Boolean.TRUE.equals(unsetFlag)) {
            if (hasOptions) sql.append(",");
            sql.append(" ").append(optionName);
            return true;
        }
        return hasOptions;
    }
    
    private boolean addOption(StringBuilder sql, boolean hasOptions, String optionName, Object value, boolean quoted) {
        if (value != null) {
            if (hasOptions) sql.append(", ");
            else sql.append(" "); // Always add space after SET
            sql.append(optionName).append(" = ");
            
            if (quoted && value instanceof String) {
                sql.append("'").append(escapeString((String) value)).append("'");
            } else {
                sql.append(value);
            }
            
            return true;
        }
        return hasOptions;
    }
    
    private String escapeString(String value) {
        if (value == null) return null;
        return value.replace("'", "''");
    }
    
    /**
     * Automatically unset format-specific options when changing format type.
     * This prevents invalid combinations like CSV-specific options on JSON formats.
     */
    private boolean addFormatTypeChangeUnsetOptions(AlterFileFormatStatement statement, StringBuilder sql, boolean hasOptions) {
        String currentType = statement.getCurrentFileFormatType();
        String newType = statement.getNewFileFormatType();
        
        // Only process if we know the current type and it's different from the new type
        if (currentType == null || newType == null || currentType.equalsIgnoreCase(newType)) {
            return hasOptions;
        }
        
        // Unset CSV-specific options when changing FROM CSV to another format
        if ("CSV".equalsIgnoreCase(currentType) && !"CSV".equalsIgnoreCase(newType)) {
            hasOptions = addFormatUnsetOption(sql, hasOptions, "RECORD_DELIMITER");
            hasOptions = addFormatUnsetOption(sql, hasOptions, "FIELD_DELIMITER");
            hasOptions = addFormatUnsetOption(sql, hasOptions, "PARSE_HEADER");
            hasOptions = addFormatUnsetOption(sql, hasOptions, "SKIP_HEADER");
            hasOptions = addFormatUnsetOption(sql, hasOptions, "SKIP_BLANK_LINES");
            hasOptions = addFormatUnsetOption(sql, hasOptions, "ESCAPE");
            hasOptions = addFormatUnsetOption(sql, hasOptions, "ESCAPE_UNENCLOSED_FIELD");
            hasOptions = addFormatUnsetOption(sql, hasOptions, "FIELD_OPTIONALLY_ENCLOSED_BY");
            hasOptions = addFormatUnsetOption(sql, hasOptions, "ERROR_ON_COLUMN_COUNT_MISMATCH");
            hasOptions = addFormatUnsetOption(sql, hasOptions, "EMPTY_FIELD_AS_NULL");
            hasOptions = addFormatUnsetOption(sql, hasOptions, "SKIP_BYTE_ORDER_MARK");
            hasOptions = addFormatUnsetOption(sql, hasOptions, "ENCODING");
        }
        
        // Unset JSON-specific options when changing FROM JSON to another format
        if ("JSON".equalsIgnoreCase(currentType) && !"JSON".equalsIgnoreCase(newType)) {
            hasOptions = addFormatUnsetOption(sql, hasOptions, "ENABLE_OCTAL");
            hasOptions = addFormatUnsetOption(sql, hasOptions, "ALLOW_DUPLICATE");
            hasOptions = addFormatUnsetOption(sql, hasOptions, "STRIP_OUTER_ARRAY");
            hasOptions = addFormatUnsetOption(sql, hasOptions, "STRIP_NULL_VALUES");
            hasOptions = addFormatUnsetOption(sql, hasOptions, "IGNORE_UTF8_ERRORS");
        }
        
        // Unset PARQUET-specific options when changing FROM PARQUET to another format
        if ("PARQUET".equalsIgnoreCase(currentType) && !"PARQUET".equalsIgnoreCase(newType)) {
            hasOptions = addFormatUnsetOption(sql, hasOptions, "SNAPPY_COMPRESSION");
            hasOptions = addFormatUnsetOption(sql, hasOptions, "BINARY_AS_TEXT");
            hasOptions = addFormatUnsetOption(sql, hasOptions, "USE_LOGICAL_TYPE");
            hasOptions = addFormatUnsetOption(sql, hasOptions, "USE_VECTORIZED_SCANNER");
        }
        
        // Unset XML-specific options when changing FROM XML to another format
        if ("XML".equalsIgnoreCase(currentType) && !"XML".equalsIgnoreCase(newType)) {
            hasOptions = addFormatUnsetOption(sql, hasOptions, "PRESERVE_SPACE");
            hasOptions = addFormatUnsetOption(sql, hasOptions, "STRIP_OUTER_ELEMENT");
            hasOptions = addFormatUnsetOption(sql, hasOptions, "DISABLE_SNOWFLAKE_DATA");
            hasOptions = addFormatUnsetOption(sql, hasOptions, "DISABLE_AUTO_CONVERT");
        }
        
        return hasOptions;
    }
    
    /**
     * Helper method to add UNSET options during format type changes.
     */
    private boolean addFormatUnsetOption(StringBuilder sql, boolean hasOptions, String optionName) {
        if (hasOptions) sql.append(", ");
        else sql.append(" ");
        sql.append(optionName);
        return true;
    }
}