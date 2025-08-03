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
        }
        
        // Add format options
        hasOptions = addOption(sql, hasOptions, "COMPRESSION", statement.getCompression(), false);
        hasOptions = addOption(sql, hasOptions, "DATE_FORMAT", statement.getDateFormat(), true);
        hasOptions = addOption(sql, hasOptions, "TIME_FORMAT", statement.getTimeFormat(), true);
        hasOptions = addOption(sql, hasOptions, "TIMESTAMP_FORMAT", statement.getTimestampFormat(), true);
        hasOptions = addOption(sql, hasOptions, "BINARY_FORMAT", statement.getBinaryFormat(), false);
        hasOptions = addOption(sql, hasOptions, "TRIM_SPACE", statement.getTrimSpace(), false);
        hasOptions = addOption(sql, hasOptions, "REPLACE_INVALID_CHARACTERS", statement.getReplaceInvalidCharacters(), false);
        hasOptions = addOption(sql, hasOptions, "FILE_EXTENSION", statement.getFileExtension(), true);
        hasOptions = addOption(sql, hasOptions, "FIELD_DELIMITER", statement.getFieldDelimiter(), true);
        hasOptions = addOption(sql, hasOptions, "SKIP_HEADER", statement.getSkipHeader(), false);
        
        // Handle NULL_IF specially
        if (statement.getNullIf() != null) {
            if (hasOptions) sql.append(" ");
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
            if (hasOptions) sql.append(" ");
            else sql.append(" "); // Always add space after SET
            sql.append("COMMENT = '").append(escapeString(statement.getNewComment())).append("'");
        }
    }
    
    private void addUnsetOptions(AlterFileFormatStatement statement, StringBuilder sql) {
        boolean hasOptions = false;
        
        // Add unset options
        if (Boolean.TRUE.equals(statement.getUnsetComment())) {
            sql.append(" COMMENT");
            hasOptions = true;
        }
        
        if (Boolean.TRUE.equals(statement.getUnsetCompression())) {
            if (hasOptions) sql.append(",");
            sql.append(" COMPRESSION");
            hasOptions = true;
        }
        
        if (Boolean.TRUE.equals(statement.getUnsetDateFormat())) {
            if (hasOptions) sql.append(",");
            sql.append(" DATE_FORMAT");
            hasOptions = true;
        }
        
        if (Boolean.TRUE.equals(statement.getUnsetTimeFormat())) {
            if (hasOptions) sql.append(",");
            sql.append(" TIME_FORMAT");
            hasOptions = true;
        }
        
        if (Boolean.TRUE.equals(statement.getUnsetTimestampFormat())) {
            if (hasOptions) sql.append(",");
            sql.append(" TIMESTAMP_FORMAT");
            hasOptions = true;
        }
        
        if (Boolean.TRUE.equals(statement.getUnsetBinaryFormat())) {
            if (hasOptions) sql.append(",");
            sql.append(" BINARY_FORMAT");
            hasOptions = true;
        }
        
        if (Boolean.TRUE.equals(statement.getUnsetTrimSpace())) {
            if (hasOptions) sql.append(",");
            sql.append(" TRIM_SPACE");
            hasOptions = true;
        }
        
        if (Boolean.TRUE.equals(statement.getUnsetNullIf())) {
            if (hasOptions) sql.append(",");
            sql.append(" NULL_IF");
            hasOptions = true;
        }
        
        if (Boolean.TRUE.equals(statement.getUnsetFileExtension())) {
            if (hasOptions) sql.append(",");
            sql.append(" FILE_EXTENSION");
            hasOptions = true;
        }
        
        // Add more unset options as needed...
    }
    
    private boolean addOption(StringBuilder sql, boolean hasOptions, String optionName, Object value, boolean quoted) {
        if (value != null) {
            if (hasOptions) sql.append(" ");
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
}