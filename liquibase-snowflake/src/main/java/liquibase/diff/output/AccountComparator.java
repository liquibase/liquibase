package liquibase.diff.output;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.Account;
import liquibase.database.object.Warehouse;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.structure.DatabaseObject;

import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Comparator for Snowflake Account objects.
 * Handles diff operations for account-level containers.
 */
public class AccountComparator implements DatabaseObjectComparator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Account.class.isAssignableFrom(objectType) && database instanceof SnowflakeDatabase) {
            return PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public String[] hash(DatabaseObject databaseObject, Database accordingTo, 
                         DatabaseObjectComparatorChain chain) {
        Account account = (Account) databaseObject;
        return new String[] { account.getName() };
    }

    @Override
    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, 
                                Database accordingTo, DatabaseObjectComparatorChain chain) {
        if (!(databaseObject1 instanceof Account && databaseObject2 instanceof Account)) {
            return false;
        }
        
        Account account1 = (Account) databaseObject1;
        Account account2 = (Account) databaseObject2;
        
        String name1 = account1.getName();
        String name2 = account2.getName();
        
        if (name1 == null || name2 == null) {
            return false;
        }
        
        // Handle case sensitivity
        if (accordingTo != null && !accordingTo.isCaseSensitive()) {
            return name1.equalsIgnoreCase(name2);
        } else {
            return name1.equals(name2);
        }
    }

    @Override
    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2,
                                           Database accordingTo, CompareControl compareControl,
                                           DatabaseObjectComparatorChain chain, Set<String> exclude) {
        
        ObjectDifferences differences = new ObjectDifferences(compareControl);
        
        if (!(databaseObject1 instanceof Account && databaseObject2 instanceof Account)) {
            return differences;
        }
        
        Account account1 = (Account) databaseObject1;
        Account account2 = (Account) databaseObject2;
        
        // Compare account-level properties
        compareProperty(differences, "region", account1.getRegion(), account2.getRegion());
        compareProperty(differences, "cloud", account1.getCloud(), account2.getCloud());
        compareProperty(differences, "accountUrl", account1.getAccountUrl(), account2.getAccountUrl());
        
        // Compare contained warehouses
        compareContainedWarehouses(account1, account2, differences, accordingTo, compareControl, chain);
        
        return differences;
    }
    
    /**
     * Compares the warehouses contained within two Account objects.
     * This method identifies added, removed, and modified warehouses.
     */
    private void compareContainedWarehouses(Account account1, Account account2, ObjectDifferences differences,
                                          Database accordingTo, CompareControl compareControl, 
                                          DatabaseObjectComparatorChain chain) {
        
        // Get warehouses from both accounts
        List<Warehouse> warehouses1 = getContainedWarehouses(account1);
        List<Warehouse> warehouses2 = getContainedWarehouses(account2);
        
        // Create maps for easier comparison
        Map<String, Warehouse> warehouseMap1 = createWarehouseMap(warehouses1, accordingTo);
        Map<String, Warehouse> warehouseMap2 = createWarehouseMap(warehouses2, accordingTo);
        
        List<Warehouse> addedWarehouses = new ArrayList<>();
        List<Warehouse> removedWarehouses = new ArrayList<>();
        List<Warehouse> modifiedWarehouses = new ArrayList<>();
        
        // Find added and modified warehouses
        for (Map.Entry<String, Warehouse> entry : warehouseMap2.entrySet()) {
            String warehouseName = entry.getKey();
            Warehouse warehouse2 = entry.getValue();
            
            if (!warehouseMap1.containsKey(warehouseName)) {
                // Warehouse exists in account2 but not account1 - it's added
                addedWarehouses.add(warehouse2);
            } else {
                // Warehouse exists in both - check if it's modified
                Warehouse warehouse1 = warehouseMap1.get(warehouseName);
                if (chain != null && isWarehouseModified(warehouse1, warehouse2, accordingTo, compareControl, chain)) {
                    modifiedWarehouses.add(warehouse2);
                }
            }
        }
        
        // Find removed warehouses
        for (Map.Entry<String, Warehouse> entry : warehouseMap1.entrySet()) {
            String warehouseName = entry.getKey();
            Warehouse warehouse1 = entry.getValue();
            
            if (!warehouseMap2.containsKey(warehouseName)) {
                // Warehouse exists in account1 but not account2 - it's removed
                removedWarehouses.add(warehouse1);
            }
        }
        
        // Add differences to the result
        if (!addedWarehouses.isEmpty()) {
            differences.addDifference("addedWarehouses", null, addedWarehouses);
        }
        if (!removedWarehouses.isEmpty()) {
            differences.addDifference("removedWarehouses", removedWarehouses, null);
        }
        if (!modifiedWarehouses.isEmpty()) {
            differences.addDifference("modifiedWarehouses", null, modifiedWarehouses);
        }
    }
    
    /**
     * Extracts the list of Warehouse objects from an Account.
     */
    private List<Warehouse> getContainedWarehouses(Account account) {
        List<Warehouse> warehouses = new ArrayList<>();
        List<DatabaseObject> objects = account.getDatabaseObjects();
        
        if (objects != null) {
            for (DatabaseObject obj : objects) {
                if (obj instanceof Warehouse) {
                    warehouses.add((Warehouse) obj);
                }
            }
        }
        
        return warehouses;
    }
    
    /**
     * Creates a map of warehouse name to Warehouse object for easier comparison.
     */
    private Map<String, Warehouse> createWarehouseMap(List<Warehouse> warehouses, Database accordingTo) {
        Map<String, Warehouse> map = new HashMap<>();
        
        for (Warehouse warehouse : warehouses) {
            String name = warehouse.getName();
            if (name != null) {
                // Handle case sensitivity
                if (accordingTo != null && !accordingTo.isCaseSensitive()) {
                    name = name.toUpperCase();
                }
                map.put(name, warehouse);
            }
        }
        
        return map;
    }
    
    /**
     * Checks if a warehouse has been modified by using the WarehouseComparator.
     */
    private boolean isWarehouseModified(Warehouse warehouse1, Warehouse warehouse2, Database accordingTo,
                                       CompareControl compareControl, DatabaseObjectComparatorChain chain) {
        try {
            ObjectDifferences warehouseDiffs = chain.findDifferences(
                warehouse1, warehouse2, accordingTo, compareControl, null
            );
            return warehouseDiffs != null && warehouseDiffs.hasDifferences();
        } catch (Exception e) {
            // If we can't compare, assume no modification
            return false;
        }
    }
    
    private void compareProperty(ObjectDifferences differences, String propertyName, Object value1, Object value2) {
        if (!java.util.Objects.equals(value1, value2)) {
            differences.addDifference(propertyName, value1, value2);
        }
    }
}