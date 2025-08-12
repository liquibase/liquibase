package liquibase.diff;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.FileFormat;
import liquibase.database.object.Stage;
import liquibase.database.object.Warehouse;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.FileFormatComparator;
import liquibase.diff.output.StageComparator;
import liquibase.diff.output.WarehouseComparator;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.structure.DatabaseObject;
import liquibase.Scope;
import liquibase.logging.Logger;

import java.util.Set;

/**
 * Simplified unified diff generator for testing the unified framework.
 * This version avoids complex fallback comparators to get the warehouse test working.
 */
public class SnowflakeExtensionDiffGeneratorSimple implements DiffGenerator {
    
    private static final Logger logger = Scope.getCurrentScope().getLog(SnowflakeExtensionDiffGeneratorSimple.class);
    
    @Override
    public int getPriority() {
        return 1000;  // Very high priority for Snowflake extension objects
    }
    
    @Override
    public boolean supports(Database referenceDatabase, Database comparisonDatabase) {
        return referenceDatabase instanceof SnowflakeDatabase || comparisonDatabase instanceof SnowflakeDatabase;
    }
    
    @Override
    public DiffResult compare(DatabaseSnapshot referenceSnapshot, DatabaseSnapshot comparisonSnapshot, 
                            CompareControl compareControl) throws DatabaseException {
        
        logger.fine("SnowflakeExtensionDiffGeneratorSimple.compare() called");
        
        // Create base diff result
        DiffResult diffResult = new DiffResult(referenceSnapshot, comparisonSnapshot, compareControl);
        
        // Handle Warehouse objects specifically
        compareWarehouses(referenceSnapshot, comparisonSnapshot, diffResult);
        
        // Handle FileFormat objects specifically
        compareFileFormats(referenceSnapshot, comparisonSnapshot, diffResult);
        
        // Handle Stage objects specifically
        compareStages(referenceSnapshot, comparisonSnapshot, diffResult);
        
        return diffResult;
    }
    
    private void compareWarehouses(DatabaseSnapshot referenceSnapshot, DatabaseSnapshot comparisonSnapshot, DiffResult diffResult) {
        CompareControl compareControl = diffResult.getCompareControl();
        logger.fine("SnowflakeExtensionDiffGeneratorSimple: Comparing Warehouse objects");
        
        Set<Warehouse> referenceWarehouses = referenceSnapshot.get(Warehouse.class);
        Set<Warehouse> comparisonWarehouses = comparisonSnapshot.get(Warehouse.class);
        
        logger.fine("SnowflakeExtensionDiffGeneratorSimple: Reference Warehouse objects: " + 
                         (referenceWarehouses != null ? referenceWarehouses.size() : "null"));
        logger.fine("SnowflakeExtensionDiffGeneratorSimple: Comparison Warehouse objects: " + 
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
                logger.fine("SnowflakeExtensionDiffGeneratorSimple: Found missing Warehouse: " + refWarehouse.getName());
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
                logger.fine("SnowflakeExtensionDiffGeneratorSimple: Found unexpected Warehouse: " + compWarehouse.getName());
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
                        logger.fine("SnowflakeExtensionDiffGeneratorSimple: Found changed Warehouse: " + refWarehouse.getName());
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
        logger.fine("SnowflakeExtensionDiffGeneratorSimple: Comparing FileFormat objects");
        
        Set<FileFormat> referenceFileFormats = referenceSnapshot.get(FileFormat.class);
        Set<FileFormat> comparisonFileFormats = comparisonSnapshot.get(FileFormat.class);
        
        logger.fine("SnowflakeExtensionDiffGeneratorSimple: Reference FileFormat objects: " + 
                         (referenceFileFormats != null ? referenceFileFormats.size() : "null"));
        logger.fine("SnowflakeExtensionDiffGeneratorSimple: Comparison FileFormat objects: " + 
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
                logger.fine("SnowflakeExtensionDiffGeneratorSimple: Found missing FileFormat: " + refFileFormat.getName());
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
                logger.fine("SnowflakeExtensionDiffGeneratorSimple: Found unexpected FileFormat: " + compFileFormat.getName());
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
                        logger.fine("SnowflakeExtensionDiffGeneratorSimple: Found changed FileFormat: " + refFileFormat.getName());
                        // Create simplified object differences for changed file format
                        diffResult.addChangedObject(refFileFormat, new liquibase.diff.ObjectDifferences(compareControl));
                    }
                    break;
                }
            }
        }
    }
    
    private void compareStages(DatabaseSnapshot referenceSnapshot, DatabaseSnapshot comparisonSnapshot, DiffResult diffResult) {
        CompareControl compareControl = diffResult.getCompareControl();
        logger.fine("SnowflakeExtensionDiffGeneratorSimple: Comparing Stage objects");
        
        Set<Stage> referenceStages = referenceSnapshot.get(Stage.class);
        Set<Stage> comparisonStages = comparisonSnapshot.get(Stage.class);
        
        logger.fine("Reference Stage objects: " + 
                         (referenceStages != null ? referenceStages.size() : "null"));
        logger.fine("Comparison Stage objects: " + 
                         (comparisonStages != null ? comparisonStages.size() : "null"));
        
        logger.fine("SnowflakeExtensionDiffGeneratorSimple: Reference Stage objects: " + 
                         (referenceStages != null ? referenceStages.size() : "null"));
        logger.fine("SnowflakeExtensionDiffGeneratorSimple: Comparison Stage objects: " + 
                         (comparisonStages != null ? comparisonStages.size() : "null"));
        
        if (referenceStages == null) referenceStages = java.util.Collections.emptySet();
        if (comparisonStages == null) comparisonStages = java.util.Collections.emptySet();
        
        StageComparator comparator = new StageComparator();
        
        // Find missing Stages (in reference but not in comparison)
        for (Stage refStage : referenceStages) {
            boolean found = false;
            for (Stage compStage : comparisonStages) {
                if (comparator.isSameObject(refStage, compStage, comparisonSnapshot.getDatabase(), null)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                logger.fine("Found missing Stage: " + refStage.getName());
                logger.fine("SnowflakeExtensionDiffGeneratorSimple: Found missing Stage: " + refStage.getName());
                diffResult.addMissingObject(refStage);
            }
        }
        
        logger.fine("Reference stages count: " + referenceStages.size() + ", Comparison stages count: " + comparisonStages.size());
        
        // Find unexpected Stages (in comparison but not in reference)  
        for (Stage compStage : comparisonStages) {
            boolean found = false;
            for (Stage refStage : referenceStages) {
                if (comparator.isSameObject(refStage, compStage, comparisonSnapshot.getDatabase(), null)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                logger.fine("SnowflakeExtensionDiffGeneratorSimple: Found unexpected Stage: " + compStage.getName());
                diffResult.addUnexpectedObject(compStage);
            }
        }
        
        // Find changed Stages (exist in both but have differences)
        for (Stage refStage : referenceStages) {
            for (Stage compStage : comparisonStages) {
                if (comparator.isSameObject(refStage, compStage, comparisonSnapshot.getDatabase(), null)) {
                    // Same stage - check for differences using simplified approach
                    boolean hasChanges = !refStage.toString().equals(compStage.toString()) ||
                                       (refStage.getUrl() != null && !refStage.getUrl().equals(compStage.getUrl())) ||
                                       (refStage.getStageType() != null && !refStage.getStageType().equals(compStage.getStageType())) ||
                                       (refStage.getStorageIntegration() != null && !refStage.getStorageIntegration().equals(compStage.getStorageIntegration())) ||
                                       (refStage.getComment() != null && !refStage.getComment().equals(compStage.getComment()));
                    
                    if (hasChanges) {
                        logger.fine("SnowflakeExtensionDiffGeneratorSimple: Found changed Stage: " + refStage.getName());
                        // Create simplified object differences for changed stage
                        diffResult.addChangedObject(refStage, new liquibase.diff.ObjectDifferences(compareControl));
                    }
                    break;
                }
            }
        }
    }
}