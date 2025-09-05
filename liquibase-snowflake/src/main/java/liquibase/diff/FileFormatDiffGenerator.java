package liquibase.diff;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.FileFormat;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.FileFormatComparator;
import liquibase.diff.ObjectDifferences;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Schema;
import liquibase.Scope;
import liquibase.logging.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

/**
 * Custom diff generator for FileFormat objects that require explicit discovery.
 * 
 * FileFormat objects are extension objects that don't support bulk discovery via addTo() method,
 * so they must be explicitly requested during diff operations.
 */
public class FileFormatDiffGenerator implements DiffGenerator {
    
    private static final Logger logger = Scope.getCurrentScope().getLog(FileFormatDiffGenerator.class);
    
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;  // High priority for Snowflake FileFormat objects
    }
    
    @Override
    public boolean supports(Database referenceDatabase, Database comparisonDatabase) {
        return referenceDatabase instanceof SnowflakeDatabase || comparisonDatabase instanceof SnowflakeDatabase;
    }
    
    @Override
    public DiffResult compare(DatabaseSnapshot referenceSnapshot, DatabaseSnapshot comparisonSnapshot, 
                            CompareControl compareControl) throws DatabaseException {
        
        logger.fine("PHASE2: FileFormatDiffGenerator.compare() called");
        
        // Create base diff result
        DiffResult diffResult = new DiffResult(referenceSnapshot, comparisonSnapshot, compareControl);
        
        // Handle FileFormat differences specifically  
        compareFileFormats(referenceSnapshot, comparisonSnapshot, compareControl, diffResult);
        
        return diffResult;
    }
    
    private void compareFileFormats(DatabaseSnapshot referenceSnapshot, DatabaseSnapshot comparisonSnapshot,
                                   CompareControl compareControl, DiffResult diffResult) throws DatabaseException {
        
        logger.fine("PHASE2: Comparing FileFormats directly...");
        
        // Discover FileFormats from both snapshots using explicit requests
        Set<FileFormat> referenceFileFormats = discoverFileFormats(referenceSnapshot);
        Set<FileFormat> comparisonFileFormats = discoverFileFormats(comparisonSnapshot);
        
        logger.fine("PHASE2: Reference FileFormats: " + (referenceFileFormats != null ? referenceFileFormats.size() : "null"));
        logger.fine("PHASE2: Comparison FileFormats: " + (comparisonFileFormats != null ? comparisonFileFormats.size() : "null"));
        
        if (referenceFileFormats == null) referenceFileFormats = new HashSet<>();
        if (comparisonFileFormats == null) comparisonFileFormats = new HashSet<>();
        
        FileFormatComparator comparator = new FileFormatComparator();
        Database database = comparisonSnapshot.getDatabase();
        
        // Find missing FileFormats (in reference but not in comparison)
        for (FileFormat refFileFormat : referenceFileFormats) {
            boolean found = false;
            for (FileFormat compFileFormat : comparisonFileFormats) {
                if (comparator.isSameObject(refFileFormat, compFileFormat, database, null)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                logger.fine("PHASE2: Found missing FileFormat: " + refFileFormat.getName());
                diffResult.addMissingObject(refFileFormat);
            }
        }
        
        // Find unexpected FileFormats (in comparison but not in reference)  
        for (FileFormat compFileFormat : comparisonFileFormats) {
            boolean found = false;
            for (FileFormat refFileFormat : referenceFileFormats) {
                if (comparator.isSameObject(refFileFormat, compFileFormat, database, null)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("🔧 PHASE2: Found unexpected FileFormat: " + compFileFormat.getName());
                diffResult.addUnexpectedObject(compFileFormat);
            }
        }
        
        // Handle changed FileFormats (same name but different properties)
        for (FileFormat refFileFormat : referenceFileFormats) {
            for (FileFormat compFileFormat : comparisonFileFormats) {
                if (comparator.isSameObject(refFileFormat, compFileFormat, database, null)) {
                    // Check if properties differ using findDifferences
                    ObjectDifferences differences = comparator.findDifferences(refFileFormat, compFileFormat, database, compareControl, null, null);
                    if (differences != null && differences.hasDifferences()) {
                        System.out.println("🔧 PHASE2: Found changed FileFormat: " + refFileFormat.getName());
                        diffResult.addChangedObject(refFileFormat, differences);
                    }
                    break;
                }
            }
        }
    }
    
    /**
     * Explicitly discover FileFormat objects from a specific database schema by querying INFORMATION_SCHEMA.
     * This bypasses the bulk discovery limitation of extension objects.
     */
    private Set<FileFormat> discoverFileFormats(DatabaseSnapshot snapshot) throws DatabaseException {
        Set<FileFormat> fileFormats = new HashSet<>();
        Database database = snapshot.getDatabase();
        
        if (!(database instanceof SnowflakeDatabase)) {
            return fileFormats;
        }
        
        // Get the current schema context from the database connection
        String currentSchema = database.getDefaultSchemaName();
        System.out.println("🔧 PHASE2: Looking for FileFormats in schema: " + currentSchema);
        
        try {
            Connection connection = database.getConnection().getUnderlyingConnection();
            // Query only the current schema to avoid cross-schema contamination
            String sql = "SELECT FILE_FORMAT_NAME, FILE_FORMAT_SCHEMA FROM INFORMATION_SCHEMA.FILE_FORMATS WHERE FILE_FORMAT_SCHEMA = ?";
            
            try (java.sql.PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, currentSchema);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String fileFormatName = rs.getString("FILE_FORMAT_NAME");
                        String schemaName = rs.getString("FILE_FORMAT_SCHEMA");
                        
                        // Create FileFormat object
                        FileFormat fileFormat = new FileFormat();
                        fileFormat.setName(fileFormatName);
                        
                        // Set schema context
                        if (schemaName != null) {
                            Schema schema = new Schema(database.getDefaultCatalogName(), schemaName);
                            fileFormat.setSchema(schema);
                        }
                        
                        // Use direct snapshot request to get full FileFormat details
                        SnapshotControl control = new SnapshotControl(database, FileFormat.class);
                        FileFormat detailedFileFormat = SnapshotGeneratorFactory.getInstance()
                            .createSnapshot(fileFormat, database, control);
                        
                        if (detailedFileFormat != null) {
                            fileFormats.add(detailedFileFormat);
                            System.out.println("🔧 PHASE2: Discovered FileFormat: " + fileFormatName + " in schema: " + schemaName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new DatabaseException("Error discovering FileFormat objects", e);
        }
        
        System.out.println("🔧 PHASE2: Total FileFormats found in schema " + currentSchema + ": " + fileFormats.size());
        return fileFormats;
    }
}