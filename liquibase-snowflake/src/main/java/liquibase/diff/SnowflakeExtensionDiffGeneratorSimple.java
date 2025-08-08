package liquibase.diff;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.FileFormat;
import liquibase.database.object.Warehouse;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.FileFormatComparator;
import liquibase.diff.output.WarehouseComparator;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.structure.DatabaseObject;

import java.util.Set;

/**
 * Simplified unified diff generator for testing the unified framework.
 * This version avoids complex fallback comparators to get the warehouse test working.
 */
public class SnowflakeExtensionDiffGeneratorSimple implements DiffGenerator {
    
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;  // High priority for Snowflake extension objects
    }
    
    @Override
    public boolean supports(Database referenceDatabase, Database comparisonDatabase) {
        return referenceDatabase instanceof SnowflakeDatabase || comparisonDatabase instanceof SnowflakeDatabase;
    }
    
    @Override
    public DiffResult compare(DatabaseSnapshot referenceSnapshot, DatabaseSnapshot comparisonSnapshot, 
                            CompareControl compareControl) throws DatabaseException {
        
        System.out.println("🔧 SnowflakeExtensionDiffGeneratorSimple.compare() called");
        
        // Create base diff result
        DiffResult diffResult = new DiffResult(referenceSnapshot, comparisonSnapshot, compareControl);
        
        // Handle Warehouse objects specifically
        compareWarehouses(referenceSnapshot, comparisonSnapshot, diffResult);
        
        // Handle FileFormat objects specifically
        compareFileFormats(referenceSnapshot, comparisonSnapshot, diffResult);
        
        return diffResult;
    }
    
    private void compareWarehouses(DatabaseSnapshot referenceSnapshot, DatabaseSnapshot comparisonSnapshot, DiffResult diffResult) {
        CompareControl compareControl = diffResult.getCompareControl();
        System.out.println("🔧 SnowflakeExtensionDiffGeneratorSimple: Comparing Warehouse objects");
        
        Set<Warehouse> referenceWarehouses = referenceSnapshot.get(Warehouse.class);
        Set<Warehouse> comparisonWarehouses = comparisonSnapshot.get(Warehouse.class);
        
        System.out.println("🔧 SnowflakeExtensionDiffGeneratorSimple: Reference Warehouse objects: " + 
                         (referenceWarehouses != null ? referenceWarehouses.size() : "null"));
        System.out.println("🔧 SnowflakeExtensionDiffGeneratorSimple: Comparison Warehouse objects: " + 
                         (comparisonWarehouses != null ? comparisonWarehouses.size() : "null"));
        
        if (referenceWarehouses == null) referenceWarehouses = java.util.Collections.emptySet();
        if (comparisonWarehouses == null) comparisonWarehouses = java.util.Collections.emptySet();
        
        WarehouseComparator comparator = new WarehouseComparator();
        
        // Find missing warehouses (in reference but not in comparison)
        for (Warehouse refWarehouse : referenceWarehouses) {
            boolean found = false;
            for (Warehouse compWarehouse : comparisonWarehouses) {
                if (comparator.isSameObject(refWarehouse, compWarehouse, comparisonSnapshot.getDatabase(), null)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("🔧 SnowflakeExtensionDiffGeneratorSimple: Found missing Warehouse: " + refWarehouse.getName());
                diffResult.addMissingObject(refWarehouse);
            }
        }
        
        // Find unexpected warehouses (in comparison but not in reference)  
        for (Warehouse compWarehouse : comparisonWarehouses) {
            boolean found = false;
            for (Warehouse refWarehouse : referenceWarehouses) {
                if (comparator.isSameObject(refWarehouse, compWarehouse, comparisonSnapshot.getDatabase(), null)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("🔧 SnowflakeExtensionDiffGeneratorSimple: Found unexpected Warehouse: " + compWarehouse.getName());
                diffResult.addUnexpectedObject(compWarehouse);
            }
        }
        
        // Find changed warehouses (exist in both but have differences)
        for (Warehouse refWarehouse : referenceWarehouses) {
            for (Warehouse compWarehouse : comparisonWarehouses) {
                if (comparator.isSameObject(refWarehouse, compWarehouse, comparisonSnapshot.getDatabase(), null)) {
                    // Same warehouse - check for differences using simplified approach
                    // For now, just detect any property differences by comparing string representations
                    boolean hasChanges = !refWarehouse.toString().equals(compWarehouse.toString()) ||
                                       (refWarehouse.getSize() != null && !refWarehouse.getSize().equals(compWarehouse.getSize())) ||
                                       (refWarehouse.getAutoSuspend() != null && !refWarehouse.getAutoSuspend().equals(compWarehouse.getAutoSuspend()));
                    
                    if (hasChanges) {
                        System.out.println("🔧 SnowflakeExtensionDiffGeneratorSimple: Found changed Warehouse: " + refWarehouse.getName());
                        // Create simplified object differences for changed warehouse
                        diffResult.addChangedObject(refWarehouse, new liquibase.diff.ObjectDifferences(compareControl));
                    }
                    break;
                }
            }
        }
    }
    
    private void compareFileFormats(DatabaseSnapshot referenceSnapshot, DatabaseSnapshot comparisonSnapshot, DiffResult diffResult) {
        CompareControl compareControl = diffResult.getCompareControl();
        System.out.println("🔧 SnowflakeExtensionDiffGeneratorSimple: Comparing FileFormat objects");
        
        Set<FileFormat> referenceFileFormats = referenceSnapshot.get(FileFormat.class);
        Set<FileFormat> comparisonFileFormats = comparisonSnapshot.get(FileFormat.class);
        
        System.out.println("🔧 SnowflakeExtensionDiffGeneratorSimple: Reference FileFormat objects: " + 
                         (referenceFileFormats != null ? referenceFileFormats.size() : "null"));
        System.out.println("🔧 SnowflakeExtensionDiffGeneratorSimple: Comparison FileFormat objects: " + 
                         (comparisonFileFormats != null ? comparisonFileFormats.size() : "null"));
        
        if (referenceFileFormats == null) referenceFileFormats = java.util.Collections.emptySet();
        if (comparisonFileFormats == null) comparisonFileFormats = java.util.Collections.emptySet();
        
        FileFormatComparator comparator = new FileFormatComparator();
        
        // Find missing FileFormats (in reference but not in comparison)
        for (FileFormat refFileFormat : referenceFileFormats) {
            boolean found = false;
            for (FileFormat compFileFormat : comparisonFileFormats) {
                if (comparator.isSameObject(refFileFormat, compFileFormat, comparisonSnapshot.getDatabase(), null)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("🔧 SnowflakeExtensionDiffGeneratorSimple: Found missing FileFormat: " + refFileFormat.getName());
                diffResult.addMissingObject(refFileFormat);
            }
        }
        
        // Find unexpected FileFormats (in comparison but not in reference)  
        for (FileFormat compFileFormat : comparisonFileFormats) {
            boolean found = false;
            for (FileFormat refFileFormat : referenceFileFormats) {
                if (comparator.isSameObject(refFileFormat, compFileFormat, comparisonSnapshot.getDatabase(), null)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("🔧 SnowflakeExtensionDiffGeneratorSimple: Found unexpected FileFormat: " + compFileFormat.getName());
                diffResult.addUnexpectedObject(compFileFormat);
            }
        }
        
        // Find changed FileFormats (exist in both but have differences)
        for (FileFormat refFileFormat : referenceFileFormats) {
            for (FileFormat compFileFormat : comparisonFileFormats) {
                if (comparator.isSameObject(refFileFormat, compFileFormat, comparisonSnapshot.getDatabase(), null)) {
                    // Same file format - check for differences using simplified approach
                    boolean hasChanges = !refFileFormat.toString().equals(compFileFormat.toString()) ||
                                       (refFileFormat.getFormatType() != null && !refFileFormat.getFormatType().equals(compFileFormat.getFormatType()));
                    
                    if (hasChanges) {
                        System.out.println("🔧 SnowflakeExtensionDiffGeneratorSimple: Found changed FileFormat: " + refFileFormat.getName());
                        // Create simplified object differences for changed file format
                        diffResult.addChangedObject(refFileFormat, new liquibase.diff.ObjectDifferences(compareControl));
                    }
                    break;
                }
            }
        }
    }
}