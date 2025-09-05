package liquibase.database.object;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;

import java.util.Objects;

/**
 * Represents a Snowflake FileFormat database object.
 * Generated from universal template with TDD enforcement.
 */
public class FileFormat extends AbstractDatabaseObject {

    private String name;
    private Schema schema;
    
    // Configuration properties - don't affect object identity
    private String formatType;  // CSV, JSON, PARQUET, etc.
    private String compression;  // AUTO, GZIP, BZ2, etc.
    
    // CSV-specific properties
    private String recordDelimiter;
    private String fieldDelimiter;
    private String fieldOptionallyEnclosedBy;  // Requirements: FIELD_OPTIONALLY_ENCLOSED_BY
    private String escape;  // Requirements: ESCAPE
    private String escapeUnenclosedField;  // Requirements: ESCAPE_UNENCLOSED_FIELD
    private Integer skipHeader;
    private Boolean skipBlankLines;
    private Boolean trimSpace;
    private Boolean emptyFieldAsNull;
    private Boolean errorOnColumnCountMismatch;
    private Boolean multiLine;  // Requirements: MULTI_LINE
    private Boolean parseHeader;  // Requirements: PARSE_HEADER
    
    // Format properties
    private String dateFormat;
    private String timeFormat;  // Requirements: TIME_FORMAT
    private String timestampFormat;
    private String binaryFormat;  // HEX, BASE64
    private String nullIf;
    
    // Additional properties from requirements
    // validateUtf8 removed - not available in Snowflake INFORMATION_SCHEMA.FILE_FORMATS
    private Boolean replaceInvalidCharacters;  // Requirements: REPLACE_INVALID_CHARACTERS
    private Boolean skipByteOrderMark;  // Requirements: SKIP_BYTE_ORDER_MARK
    private String encoding;  // Requirements: ENCODING
    private String fileExtension;  // Requirements: FILE_EXTENSION
    
    // Backward compatibility - keep these for existing tests
    private String quoteCharacter;  // Maps to fieldOptionallyEnclosedBy
    private String escapeCharacter;  // Maps to escape

    public FileFormat() {
        super();
    }

    public FileFormat(String name) {
        this();
        setName(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public FileFormat setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

    public FileFormat setSchema(Schema schema) {
        this.schema = schema;
        return this;
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        return schema != null ? new DatabaseObject[] { schema } : null;
    }

    public String getFormatType() {
        return formatType;
    }
    
    public FileFormat setFormatType(String formatType) {
        this.formatType = formatType;
        return this;
    }
    
    // Backward compatibility methods for existing tests
    public String getType() {
        return formatType;
    }
    
    public FileFormat setType(String type) {
        this.formatType = type;
        return this;
    }
    
    public String getCompression() {
        return compression;
    }
    
    public FileFormat setCompression(String compression) {
        this.compression = compression;
        return this;
    }
    
    public String getRecordDelimiter() {
        return recordDelimiter;
    }
    
    public FileFormat setRecordDelimiter(String recordDelimiter) {
        this.recordDelimiter = recordDelimiter;
        return this;
    }
    
    public String getFieldDelimiter() {
        return fieldDelimiter;
    }
    
    public FileFormat setFieldDelimiter(String fieldDelimiter) {
        this.fieldDelimiter = fieldDelimiter;
        return this;
    }
    
    public String getQuoteCharacter() {
        return quoteCharacter;
    }
    
    public FileFormat setQuoteCharacter(String quoteCharacter) {
        this.quoteCharacter = quoteCharacter;
        return this;
    }
    
    public String getEscapeCharacter() {
        return escapeCharacter;
    }
    
    public FileFormat setEscapeCharacter(String escapeCharacter) {
        this.escapeCharacter = escapeCharacter;
        return this;
    }
    
    public Integer getSkipHeader() {
        return skipHeader;
    }
    
    public FileFormat setSkipHeader(Integer skipHeader) {
        this.skipHeader = skipHeader;
        return this;
    }
    
    public Boolean getSkipBlankLines() {
        return skipBlankLines;
    }
    
    public FileFormat setSkipBlankLines(Boolean skipBlankLines) {
        this.skipBlankLines = skipBlankLines;
        return this;
    }
    
    public Boolean getTrimSpace() {
        return trimSpace;
    }
    
    public FileFormat setTrimSpace(Boolean trimSpace) {
        this.trimSpace = trimSpace;
        return this;
    }
    
    public Boolean getEmptyFieldAsNull() {
        return emptyFieldAsNull;
    }
    
    public FileFormat setEmptyFieldAsNull(Boolean emptyFieldAsNull) {
        this.emptyFieldAsNull = emptyFieldAsNull;
        return this;
    }
    
    public Boolean getErrorOnColumnCountMismatch() {
        return errorOnColumnCountMismatch;
    }
    
    public FileFormat setErrorOnColumnCountMismatch(Boolean errorOnColumnCountMismatch) {
        this.errorOnColumnCountMismatch = errorOnColumnCountMismatch;
        return this;
    }
    
    public String getDateFormat() {
        return dateFormat;
    }
    
    public FileFormat setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        return this;
    }
    
    public String getTimestampFormat() {
        return timestampFormat;
    }
    
    public FileFormat setTimestampFormat(String timestampFormat) {
        this.timestampFormat = timestampFormat;
        return this;
    }
    
    public String getBinaryFormat() {
        return binaryFormat;
    }
    
    public FileFormat setBinaryFormat(String binaryFormat) {
        this.binaryFormat = binaryFormat;
        return this;
    }
    
    public String getNullIf() {
        return nullIf;
    }
    
    public FileFormat setNullIf(String nullIf) {
        this.nullIf = nullIf;
        return this;
    }
    
    // Additional properties from requirements - getters and setters
    
    public String getFieldOptionallyEnclosedBy() {
        return fieldOptionallyEnclosedBy;
    }
    
    public FileFormat setFieldOptionallyEnclosedBy(String fieldOptionallyEnclosedBy) {
        this.fieldOptionallyEnclosedBy = fieldOptionallyEnclosedBy;
        return this;
    }
    
    public String getEscape() {
        return escape;
    }
    
    public FileFormat setEscape(String escape) {
        this.escape = escape;
        return this;
    }
    
    public String getEscapeUnenclosedField() {
        return escapeUnenclosedField;
    }
    
    public FileFormat setEscapeUnenclosedField(String escapeUnenclosedField) {
        this.escapeUnenclosedField = escapeUnenclosedField;
        return this;
    }
    
    public Boolean getMultiLine() {
        return multiLine;
    }
    
    public FileFormat setMultiLine(Boolean multiLine) {
        this.multiLine = multiLine;
        return this;
    }
    
    public Boolean getParseHeader() {
        return parseHeader;
    }
    
    public FileFormat setParseHeader(Boolean parseHeader) {
        this.parseHeader = parseHeader;
        return this;
    }
    
    public String getTimeFormat() {
        return timeFormat;
    }
    
    public FileFormat setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
        return this;
    }
    
    // validateUtf8 getter/setter removed - not available in Snowflake INFORMATION_SCHEMA.FILE_FORMATS
    
    public Boolean getReplaceInvalidCharacters() {
        return replaceInvalidCharacters;
    }
    
    public FileFormat setReplaceInvalidCharacters(Boolean replaceInvalidCharacters) {
        this.replaceInvalidCharacters = replaceInvalidCharacters;
        return this;
    }
    
    public Boolean getSkipByteOrderMark() {
        return skipByteOrderMark;
    }
    
    public FileFormat setSkipByteOrderMark(Boolean skipByteOrderMark) {
        this.skipByteOrderMark = skipByteOrderMark;
        return this;
    }
    
    public String getEncoding() {
        return encoding;
    }
    
    public FileFormat setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }
    
    public String getFileExtension() {
        return fileExtension;
    }
    
    public FileFormat setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        FileFormat that = (FileFormat) obj;
        return Objects.equals(name, that.name) &&
               Objects.equals(schema, that.schema);
               // Property equals checks added via TDD micro-cycles;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, schema);
        // Property hash fields added via TDD micro-cycles);
    }

    @Override
    public String toString() {
        return "FileFormat{" +
               "name='" + name + '\'' +
               ", schema=" + schema +
               // Property toString fields added via TDD micro-cycles
               '}';
    }
}