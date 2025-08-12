package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.object.FileFormat;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.Scope;
import liquibase.logging.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Snowflake-specific FileFormat snapshot generator.
 * Queries Snowflake INFORMATION_SCHEMA for FileFormat objects.
 */
public class FileFormatSnapshotGeneratorSnowflake extends JdbcSnapshotGenerator {
    
    private static final Logger logger = Scope.getCurrentScope().getLog(FileFormatSnapshotGeneratorSnowflake.class);
    
    // Cache for FileFormats within a single snapshot operation to avoid repeated queries
    private final Map<String, Set<FileFormat>> schemaFileFormatsCache = new ConcurrentHashMap<>();

    public FileFormatSnapshotGeneratorSnowflake() {
        super(FileFormat.class, new Class[]{Schema.class});
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!(example instanceof FileFormat)) {
            return null;
        }
        
        Database database = snapshot.getDatabase();
        FileFormat fileFormat = (FileFormat) example;
        
        if (fileFormat.getName() == null) {
            return null;
        }
        
        Schema schema = fileFormat.getSchema();
        if (schema == null) {
            Catalog catalog = new Catalog(database.getDefaultCatalogName());
            schema = new Schema(catalog, database.getDefaultSchemaName());
        }
        
        try {
            JdbcConnection connection = (JdbcConnection) database.getConnection();
            
            // Query INFORMATION_SCHEMA.FILE_FORMATS - Actual columns verified against real Snowflake
            String sql = "SELECT " +
                "FILE_FORMAT_CATALOG, " +
                "FILE_FORMAT_SCHEMA, " +
                "FILE_FORMAT_NAME, " +
                "FILE_FORMAT_OWNER, " +
                "FILE_FORMAT_TYPE, " +
                "RECORD_DELIMITER, " +
                "FIELD_DELIMITER, " +
                "SKIP_HEADER, " +
                "DATE_FORMAT, " +
                "TIME_FORMAT, " +
                "TIMESTAMP_FORMAT, " +
                "BINARY_FORMAT, " +
                "ESCAPE, " +
                "ESCAPE_UNENCLOSED_FIELD, " +
                "TRIM_SPACE, " +
                "FIELD_OPTIONALLY_ENCLOSED_BY, " +
                "NULL_IF, " +
                "COMPRESSION, " +
                "ERROR_ON_COLUMN_COUNT_MISMATCH, " +
                "CREATED, " +
                "LAST_ALTERED, " +
                "COMMENT " +
                "FROM INFORMATION_SCHEMA.FILE_FORMATS " +
                "WHERE FILE_FORMAT_NAME = ?" +
                (schema != null ? " AND FILE_FORMAT_SCHEMA = ?" : "");
                
            try (Statement stmt = connection.createStatement()) {
                String escapedFormatName = database.escapeStringForDatabase(fileFormat.getName());
                String fullSql = sql.replaceFirst("\\?", "'" + escapedFormatName + "'");
                
                if (schema != null) {
                    String escapedSchemaName = database.escapeStringForDatabase(schema.getName());
                    fullSql = fullSql.replaceFirst("\\?", "'" + escapedSchemaName + "'");
                }
                
                try (ResultSet rs = stmt.executeQuery(fullSql)) {
                    if (rs.next()) {
                        FileFormat returnFormat = new FileFormat();
                        returnFormat.setName(rs.getString("FILE_FORMAT_NAME"));
                        returnFormat.setSchema(schema);
                        
                        // Set all properties from INFORMATION_SCHEMA per requirements
                        returnFormat.setFormatType(rs.getString("FILE_FORMAT_TYPE"));
                        returnFormat.setRecordDelimiter(rs.getString("RECORD_DELIMITER"));
                        returnFormat.setFieldDelimiter(rs.getString("FIELD_DELIMITER"));
                        returnFormat.setDateFormat(rs.getString("DATE_FORMAT"));
                        returnFormat.setTimeFormat(rs.getString("TIME_FORMAT"));
                        returnFormat.setTimestampFormat(rs.getString("TIMESTAMP_FORMAT"));
                        returnFormat.setBinaryFormat(rs.getString("BINARY_FORMAT"));
                        returnFormat.setCompression(rs.getString("COMPRESSION"));
                        returnFormat.setNullIf(rs.getString("NULL_IF"));
                        // Properties verified to exist in Snowflake INFORMATION_SCHEMA
                        returnFormat.setEscape(rs.getString("ESCAPE"));
                        returnFormat.setEscapeUnenclosedField(rs.getString("ESCAPE_UNENCLOSED_FIELD"));
                        returnFormat.setFieldOptionallyEnclosedBy(rs.getString("FIELD_OPTIONALLY_ENCLOSED_BY"));
                        
                        // Handle integer and boolean fields
                        int skipHeader = rs.getInt("SKIP_HEADER");
                        if (!rs.wasNull()) {
                            returnFormat.setSkipHeader(skipHeader);
                        }
                        
                        String trimSpace = rs.getString("TRIM_SPACE");
                        if (trimSpace != null) {
                            returnFormat.setTrimSpace("TRUE".equalsIgnoreCase(trimSpace));
                        }
                        
                        String errorOnColumnCountMismatch = rs.getString("ERROR_ON_COLUMN_COUNT_MISMATCH");
                        if (errorOnColumnCountMismatch != null) {
                            returnFormat.setErrorOnColumnCountMismatch("TRUE".equalsIgnoreCase(errorOnColumnCountMismatch));
                        }
                        
                        // Note: The following properties from requirements document don't exist in actual Snowflake:
                        // VALIDATE_UTF8, SKIP_BLANK_LINES, REPLACE_INVALID_CHARACTERS, EMPTY_FIELD_AS_NULL,
                        // SKIP_BYTE_ORDER_MARK, ENCODING, MULTI_LINE, PARSE_HEADER, FILE_EXTENSION
                        // These remain as object properties for potential future Snowflake versions
                        
                        return returnFormat;
                    } else {
                        return null;
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying FileFormat: " + fileFormat.getName(), e);
        }
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        
        logger.fine("FileFormatSnapshotGenerator.addTo() called with foundObject: " + 
                         (foundObject != null ? foundObject.getClass().getSimpleName() + 
                          " name=" + (foundObject instanceof Schema ? ((Schema)foundObject).getName() : "N/A") : "null"));
        
        if (!snapshot.getSnapshotControl().shouldInclude(FileFormat.class)) {
            logger.fine("FileFormatSnapshotGenerator: FileFormat class not included in snapshot control");
            return;
        }

        if (foundObject instanceof Schema) {
            Schema schema = (Schema) foundObject;
            Database database = snapshot.getDatabase();
            
            logger.fine("FileFormatSnapshotGenerator: Processing Schema '" + schema.getName() + "' for FileFormat discovery");
            
            if (!(database instanceof SnowflakeDatabase)) {
                logger.fine("FileFormatSnapshotGenerator: Database is not Snowflake");
                return;
            }
            
            try {
                logger.fine("FileFormatSnapshotGenerator: Starting bulk FileFormat discovery for schema");
                addAllFileFormats(schema, database, snapshot);
                logger.fine("FileFormatSnapshotGenerator: Completed FileFormat discovery for schema");
            } catch (SQLException e) {
                logger.warning("FileFormatSnapshotGenerator: Error discovering FileFormats: " + e.getMessage());
                throw new DatabaseException("Error discovering FileFormats: " + e.getMessage(), e);
            }
        } else {
            logger.fine("FileFormatSnapshotGenerator: foundObject is not Schema (" + 
                             (foundObject != null ? foundObject.getClass().getSimpleName() : "null") + ")");
        }
    }

    private void addAllFileFormats(Schema schema, Database database, DatabaseSnapshot snapshot) throws SQLException, DatabaseException {
        logger.fine("FileFormatSnapshotGenerator: Executing INFORMATION_SCHEMA.FILE_FORMATS query for schema: " + schema.getName());
        
        String schemaName = schema.getName();
        String catalogName = schema.getCatalogName();
        
        // Initialize variables for caching and tracking
        String schemaKey = catalogName + "." + schemaName;
        Set<FileFormat> discoveredFileFormats = new HashSet<>();
        
        // Query INFORMATION_SCHEMA.FILE_FORMATS for all FileFormats in this schema
        String sql = "SELECT " +
            "FILE_FORMAT_CATALOG, " +
            "FILE_FORMAT_SCHEMA, " +
            "FILE_FORMAT_NAME, " +
            "FILE_FORMAT_OWNER, " +
            "FILE_FORMAT_TYPE, " +
            "RECORD_DELIMITER, " +
            "FIELD_DELIMITER, " +
            "SKIP_HEADER, " +
            "DATE_FORMAT, " +
            "TIME_FORMAT, " +
            "TIMESTAMP_FORMAT, " +
            "BINARY_FORMAT, " +
            "ESCAPE, " +
            "ESCAPE_UNENCLOSED_FIELD, " +
            "TRIM_SPACE, " +
            "FIELD_OPTIONALLY_ENCLOSED_BY, " +
            "ERROR_ON_COLUMN_COUNT_MISMATCH, " +
            "COMPRESSION, " +
            "NULL_IF, " +
            "COMMENT " +
            "FROM INFORMATION_SCHEMA.FILE_FORMATS " +
            "WHERE FILE_FORMAT_CATALOG = ? AND FILE_FORMAT_SCHEMA = ?";
            
        PreparedStatement stmt = ((JdbcConnection) database.getConnection()).prepareStatement(sql);
        stmt.setString(1, catalogName);
        stmt.setString(2, schemaName);
        
        ResultSet rs = stmt.executeQuery();
        
        int fileFormatCount = 0;
        while (rs.next()) {
            String fileFormatName = rs.getString("FILE_FORMAT_NAME");
            fileFormatCount++;
            
            logger.fine("FileFormatSnapshotGenerator: Found FileFormat #" + fileFormatCount + ": " + fileFormatName);
            
            // Create FileFormat object for each discovered file format
            FileFormat fileFormatObject = new FileFormat();
            fileFormatObject.setName(fileFormatName);
            fileFormatObject.setSchema(schema);
            
            // Set comprehensive attributes from INFORMATION_SCHEMA.FILE_FORMATS
            String formatType = rs.getString("FILE_FORMAT_TYPE");
            if (formatType != null) {
                fileFormatObject.setFormatType(formatType);
            }
            
            String fieldDelimiter = rs.getString("FIELD_DELIMITER");
            if (fieldDelimiter != null) {
                fileFormatObject.setFieldDelimiter(fieldDelimiter);
            }
            
            String recordDelimiter = rs.getString("RECORD_DELIMITER");
            if (recordDelimiter != null) {
                fileFormatObject.setRecordDelimiter(recordDelimiter);
            }
            
            int skipHeader = rs.getInt("SKIP_HEADER");
            if (!rs.wasNull()) {
                fileFormatObject.setSkipHeader(skipHeader);
            }
            
            String dateFormat = rs.getString("DATE_FORMAT");
            if (dateFormat != null) {
                fileFormatObject.setDateFormat(dateFormat);
            }
            
            String timeFormat = rs.getString("TIME_FORMAT");
            if (timeFormat != null) {
                fileFormatObject.setTimeFormat(timeFormat);
            }
            
            String timestampFormat = rs.getString("TIMESTAMP_FORMAT");
            if (timestampFormat != null) {
                fileFormatObject.setTimestampFormat(timestampFormat);
            }
            
            String binaryFormat = rs.getString("BINARY_FORMAT");
            if (binaryFormat != null) {
                fileFormatObject.setBinaryFormat(binaryFormat);
            }
            
            String escape = rs.getString("ESCAPE");
            if (escape != null) {
                fileFormatObject.setEscape(escape);
            }
            
            String escapeUnenclosedField = rs.getString("ESCAPE_UNENCLOSED_FIELD");
            if (escapeUnenclosedField != null) {
                fileFormatObject.setEscapeUnenclosedField(escapeUnenclosedField);
            }
            
            String trimSpaceStr = rs.getString("TRIM_SPACE");
            if (trimSpaceStr != null) {
                fileFormatObject.setTrimSpace(convertYesNoToBoolean(trimSpaceStr));
            }
            
            String fieldOptionallyEnclosedBy = rs.getString("FIELD_OPTIONALLY_ENCLOSED_BY");
            if (fieldOptionallyEnclosedBy != null) {
                fileFormatObject.setFieldOptionallyEnclosedBy(fieldOptionallyEnclosedBy);
            }
            
            String errorOnColumnCountMismatchStr = rs.getString("ERROR_ON_COLUMN_COUNT_MISMATCH");
            if (errorOnColumnCountMismatchStr != null) {
                fileFormatObject.setErrorOnColumnCountMismatch(convertYesNoToBoolean(errorOnColumnCountMismatchStr));
            }
            
            String compression = rs.getString("COMPRESSION");
            if (compression != null) {
                fileFormatObject.setCompression(compression);
            }
            
            String nullIf = rs.getString("NULL_IF");
            if (nullIf != null && !"null".equalsIgnoreCase(nullIf)) {
                // Store the raw NULL_IF string - FileFormat expects String not List
                fileFormatObject.setNullIf(nullIf);
            }
            
            // Note: FileFormat class doesn't have setComment() method yet
            // String comment = rs.getString("COMMENT");
            // This would need to be added to FileFormat class if needed for bulk discovery
            
            logger.fine("FileFormatSnapshotGenerator: Adding FileFormat '" + fileFormatName + "' to schema '" + schema.getName() + "'");
            
            // Add to discovered set for caching and to schema
            discoveredFileFormats.add(fileFormatObject);
            schema.addDatabaseObject(fileFormatObject);
            
            // CRITICAL FIX: Also add FileFormat to top-level snapshot for diff access
            // This enables snapshot.get(FileFormat.class) to find FileFormats for changelog generation
            try {
                logger.fine("FileFormatSnapshotGenerator: Adding FileFormat '" + fileFormatName + "' to top-level snapshot for diff access");
                snapshot.include(fileFormatObject);
            } catch (InvalidExampleException e) {
                logger.warning("FileFormatSnapshotGenerator: Could not add FileFormat to top-level snapshot: " + e.getMessage());
            }
        }
        
        rs.close();
        stmt.close();
        
        // Cache the results for future queries within this snapshot session
        schemaFileFormatsCache.put(schemaKey, discoveredFileFormats);
        logger.fine("FileFormatSnapshotGenerator: Discovered " + fileFormatCount + " FileFormats in schema " + schema.getName());
    }
    
    // Helper method to reduce verbosity when setting properties
    private void setIfNotNull(Consumer<String> setter, String value) {
        if (value != null) {
            setter.accept(value);
        }
    }
    
    // Clear cache when snapshot completes
    public void clearCache() {
        schemaFileFormatsCache.clear();
    }
    
    private Boolean convertYesNoToBoolean(String value) {
        if (value == null) return null;
        return "YES".equalsIgnoreCase(value) || "TRUE".equalsIgnoreCase(value) || "1".equals(value);
    }

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (database instanceof SnowflakeDatabase) {
            if (FileFormat.class.isAssignableFrom(objectType)) {
                return PRIORITY_DATABASE;
            }
            // FileFormat generator adds to Schema objects (as specified in constructor)
            if (Schema.class.isAssignableFrom(objectType)) {
                return PRIORITY_ADDITIONAL;
            }
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends SnapshotGenerator>[] replaces() {
        return new Class[0];
    }

    // Helper methods will be added via TDD micro-cycles
    // Helper methods added via TDD micro-cycles
}