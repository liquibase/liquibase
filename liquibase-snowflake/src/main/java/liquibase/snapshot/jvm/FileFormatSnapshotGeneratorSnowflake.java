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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Snowflake-specific FileFormat snapshot generator.
 * Queries Snowflake INFORMATION_SCHEMA for FileFormat objects.
 */
public class FileFormatSnapshotGeneratorSnowflake extends JdbcSnapshotGenerator {

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
            
            // Query INFORMATION_SCHEMA.FILE_FORMATS
            String sql = "SELECT " +
                "FILE_FORMAT_NAME, " +
                "FILE_FORMAT_TYPE, " +
                "RECORD_DELIMITER, " +
                "FIELD_DELIMITER, " +
                "QUOTE_CHARACTER, " +
                "ESCAPE_CHARACTER, " +
                "DATE_FORMAT, " +
                "TIMESTAMP_FORMAT, " +
                "BINARY_FORMAT, " +
                "COMPRESSION, " +
                "NULL_IF, " +
                "SKIP_HEADER, " +
                "SKIP_BLANK_LINES, " +
                "TRIM_SPACE, " +
                "ERROR_ON_COLUMN_COUNT_MISMATCH, " +
                "EMPTY_FIELD_AS_NULL " +
                "FROM INFORMATION_SCHEMA.FILE_FORMATS " +
                "WHERE FILE_FORMAT_NAME = ?" +
                (schema != null ? " AND FILE_FORMAT_SCHEMA = ?" : "");
                
            try (Statement stmt = connection.createStatement()) {
                String escapedFormatName = database.escapeStringForDatabase(fileFormat.getName());
                String fullSql = sql.replace("?", "'" + escapedFormatName + "'");
                
                if (schema != null) {
                    String escapedSchemaName = database.escapeStringForDatabase(schema.getName());
                    fullSql = fullSql.replaceFirst("\\?", "'" + escapedSchemaName + "'");
                }
                
                try (ResultSet rs = stmt.executeQuery(fullSql)) {
                    if (rs.next()) {
                        FileFormat returnFormat = new FileFormat();
                        returnFormat.setName(rs.getString("FILE_FORMAT_NAME"));
                        returnFormat.setSchema(schema);
                        
                        // Set all properties from INFORMATION_SCHEMA
                        returnFormat.setFormatType(rs.getString("FILE_FORMAT_TYPE"));
                        returnFormat.setRecordDelimiter(rs.getString("RECORD_DELIMITER"));
                        returnFormat.setFieldDelimiter(rs.getString("FIELD_DELIMITER"));
                        returnFormat.setQuoteCharacter(rs.getString("QUOTE_CHARACTER"));
                        returnFormat.setEscapeCharacter(rs.getString("ESCAPE_CHARACTER"));
                        returnFormat.setDateFormat(rs.getString("DATE_FORMAT"));
                        returnFormat.setTimestampFormat(rs.getString("TIMESTAMP_FORMAT"));
                        returnFormat.setBinaryFormat(rs.getString("BINARY_FORMAT"));
                        returnFormat.setCompression(rs.getString("COMPRESSION"));
                        returnFormat.setNullIf(rs.getString("NULL_IF"));
                        
                        // Handle integer and boolean fields
                        int skipHeader = rs.getInt("SKIP_HEADER");
                        if (!rs.wasNull()) {
                            returnFormat.setSkipHeader(skipHeader);
                        }
                        
                        String skipBlankLines = rs.getString("SKIP_BLANK_LINES");
                        if (skipBlankLines != null) {
                            returnFormat.setSkipBlankLines("TRUE".equalsIgnoreCase(skipBlankLines));
                        }
                        
                        String trimSpace = rs.getString("TRIM_SPACE");
                        if (trimSpace != null) {
                            returnFormat.setTrimSpace("TRUE".equalsIgnoreCase(trimSpace));
                        }
                        
                        String errorOnColumnCountMismatch = rs.getString("ERROR_ON_COLUMN_COUNT_MISMATCH");
                        if (errorOnColumnCountMismatch != null) {
                            returnFormat.setErrorOnColumnCountMismatch("TRUE".equalsIgnoreCase(errorOnColumnCountMismatch));
                        }
                        
                        String emptyFieldAsNull = rs.getString("EMPTY_FIELD_AS_NULL");
                        if (emptyFieldAsNull != null) {
                            returnFormat.setEmptyFieldAsNull("TRUE".equalsIgnoreCase(emptyFieldAsNull));
                        }
                        
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
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException {
        if (!(foundObject instanceof Schema)) {
            return;
        }
        
        Schema schema = (Schema) foundObject;
        Database database = snapshot.getDatabase();
        
        try {
            JdbcConnection connection = (JdbcConnection) database.getConnection();
            
            // Query all FileFormats in the schema
            String sql = "SELECT " +
                "FILE_FORMAT_NAME, " +
                "FILE_FORMAT_TYPE, " +
                "RECORD_DELIMITER, " +
                "FIELD_DELIMITER, " +
                "QUOTE_CHARACTER, " +
                "ESCAPE_CHARACTER, " +
                "DATE_FORMAT, " +
                "TIMESTAMP_FORMAT, " +
                "BINARY_FORMAT, " +
                "COMPRESSION, " +
                "NULL_IF, " +
                "SKIP_HEADER, " +
                "SKIP_BLANK_LINES, " +
                "TRIM_SPACE, " +
                "ERROR_ON_COLUMN_COUNT_MISMATCH, " +
                "EMPTY_FIELD_AS_NULL " +
                "FROM INFORMATION_SCHEMA.FILE_FORMATS " +
                "WHERE FILE_FORMAT_SCHEMA = ?";
                
            try (Statement stmt = connection.createStatement()) {
                String escapedSchemaName = database.escapeStringForDatabase(schema.getName());
                String fullSql = sql.replace("?", "'" + escapedSchemaName + "'");
                
                try (ResultSet rs = stmt.executeQuery(fullSql)) {
                    while (rs.next()) {
                        FileFormat fileFormat = new FileFormat();
                        fileFormat.setName(rs.getString("FILE_FORMAT_NAME"));
                        fileFormat.setSchema(schema);
                        
                        // Set all properties from INFORMATION_SCHEMA
                        fileFormat.setFormatType(rs.getString("FILE_FORMAT_TYPE"));
                        fileFormat.setRecordDelimiter(rs.getString("RECORD_DELIMITER"));
                        fileFormat.setFieldDelimiter(rs.getString("FIELD_DELIMITER"));
                        fileFormat.setQuoteCharacter(rs.getString("QUOTE_CHARACTER"));
                        fileFormat.setEscapeCharacter(rs.getString("ESCAPE_CHARACTER"));
                        fileFormat.setDateFormat(rs.getString("DATE_FORMAT"));
                        fileFormat.setTimestampFormat(rs.getString("TIMESTAMP_FORMAT"));
                        fileFormat.setBinaryFormat(rs.getString("BINARY_FORMAT"));
                        fileFormat.setCompression(rs.getString("COMPRESSION"));
                        fileFormat.setNullIf(rs.getString("NULL_IF"));
                        
                        // Handle integer and boolean fields
                        int skipHeader = rs.getInt("SKIP_HEADER");
                        if (!rs.wasNull()) {
                            fileFormat.setSkipHeader(skipHeader);
                        }
                        
                        String skipBlankLines = rs.getString("SKIP_BLANK_LINES");
                        if (skipBlankLines != null) {
                            fileFormat.setSkipBlankLines("TRUE".equalsIgnoreCase(skipBlankLines));
                        }
                        
                        String trimSpace = rs.getString("TRIM_SPACE");
                        if (trimSpace != null) {
                            fileFormat.setTrimSpace("TRUE".equalsIgnoreCase(trimSpace));
                        }
                        
                        String errorOnColumnCountMismatch = rs.getString("ERROR_ON_COLUMN_COUNT_MISMATCH");
                        if (errorOnColumnCountMismatch != null) {
                            fileFormat.setErrorOnColumnCountMismatch("TRUE".equalsIgnoreCase(errorOnColumnCountMismatch));
                        }
                        
                        String emptyFieldAsNull = rs.getString("EMPTY_FIELD_AS_NULL");
                        if (emptyFieldAsNull != null) {
                            fileFormat.setEmptyFieldAsNull("TRUE".equalsIgnoreCase(emptyFieldAsNull));
                        }
                        
                        schema.addDatabaseObject(fileFormat);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying FileFormats for schema: " + schema.getName(), e);
        }
    }

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (database instanceof SnowflakeDatabase && FileFormat.class.isAssignableFrom(objectType)) {
            return PRIORITY_DATABASE;
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