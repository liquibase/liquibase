package liquibase.diff;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.Warehouse;
import liquibase.database.object.FileFormat;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.WarehouseComparator;
import liquibase.diff.output.FileFormatComparator;
import liquibase.exception.DatabaseException;
import liquibase.servicelocator.PrioritizedService;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.structure.DatabaseObject;

import java.util.Set;

/**
 * Custom diff generator for account-level objects (Warehouses, etc.) that don't have schema context.
 * 
 * This works around Liquibase's schema-centric diff assumptions by providing direct object comparison
 * for account-level objects that return null from getSchema().
 */
public class AccountLevelDiffGenerator implements DiffGenerator {
    
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;  // High priority for Snowflake account-level objects
    }
    
    @Override
    public boolean supports(Database referenceDatabase, Database comparisonDatabase) {
        return referenceDatabase instanceof SnowflakeDatabase || comparisonDatabase instanceof SnowflakeDatabase;
    }
    
    @Override
    public DiffResult compare(DatabaseSnapshot referenceSnapshot, DatabaseSnapshot comparisonSnapshot, 
                            CompareControl compareControl) throws DatabaseException {
        
        System.out.println("🔧 PHASE2: AccountLevelDiffGenerator.compare() called");
        
        // Create base diff result
        DiffResult diffResult = new DiffResult(referenceSnapshot, comparisonSnapshot, compareControl);
        
        // Handle warehouse differences specifically  
        compareWarehouses(referenceSnapshot, comparisonSnapshot, compareControl, diffResult);
        
        // TODO: Add other account-level objects (roles, users, etc.) here in future
        
        return diffResult;
    }
    
    private void compareWarehouses(DatabaseSnapshot referenceSnapshot, DatabaseSnapshot comparisonSnapshot,
                                 CompareControl compareControl, DiffResult diffResult) {
        
        System.out.println("🔧 PHASE2: Comparing warehouses directly...");
        
        Set<Warehouse> referenceWarehouses = referenceSnapshot.get(Warehouse.class);
        Set<Warehouse> comparisonWarehouses = comparisonSnapshot.get(Warehouse.class);
        
        System.out.println("🔧 PHASE2: Reference warehouses: " + (referenceWarehouses != null ? referenceWarehouses.size() : "null"));
        System.out.println("🔧 PHASE2: Comparison warehouses: " + (comparisonWarehouses != null ? comparisonWarehouses.size() : "null"));
        
        if (referenceWarehouses == null) referenceWarehouses = java.util.Collections.emptySet();
        if (comparisonWarehouses == null) comparisonWarehouses = java.util.Collections.emptySet();
        
        WarehouseComparator comparator = new WarehouseComparator();
        Database database = comparisonSnapshot.getDatabase();
        
        // Find missing warehouses (in reference but not in comparison)
        for (Warehouse refWarehouse : referenceWarehouses) {
            boolean found = false;
            for (Warehouse compWarehouse : comparisonWarehouses) {
                if (comparator.isSameObject(refWarehouse, compWarehouse, database, null)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("🔧 PHASE2: Found missing warehouse: " + refWarehouse.getName());
                diffResult.addMissingObject(refWarehouse);
            }
        }
        
        // Find unexpected warehouses (in comparison but not in reference)  
        for (Warehouse compWarehouse : comparisonWarehouses) {
            boolean found = false;
            for (Warehouse refWarehouse : referenceWarehouses) {
                if (comparator.isSameObject(refWarehouse, compWarehouse, database, null)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("🔧 PHASE2: Found unexpected warehouse: " + compWarehouse.getName());
                diffResult.addUnexpectedObject(compWarehouse);
            }
        }
        
        // TODO: Handle changed warehouses (same name but different properties)
        // This would require calling comparator.findDifferences() for matched pairs
    }
}