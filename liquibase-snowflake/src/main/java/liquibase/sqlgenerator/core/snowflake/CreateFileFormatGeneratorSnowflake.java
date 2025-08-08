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
        
        // Use the enhanced validation from the statement
        CreateFileFormatStatement.ValidationResult result = statement.validate();
        
        // Convert validation result to Liquibase ValidationErrors
        for (String error : result.getErrors()) {
            errors.addError(error);
        }
        
        for (String warning : result.getWarnings()) {
            errors.addWarning(warning);
        }
        
        return errors;
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