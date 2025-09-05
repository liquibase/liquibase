package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.object.Account;
import liquibase.database.object.Warehouse;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.Scope;
import liquibase.logging.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Snowflake-specific warehouse snapshot generator.
 * Creates proper Warehouse objects within Account containers.
 */
public class WarehouseSnapshotGeneratorSnowflake extends JdbcSnapshotGenerator {

    private static final Logger logger = Scope.getCurrentScope().getLog(WarehouseSnapshotGeneratorSnowflake.class);

    public WarehouseSnapshotGeneratorSnowflake() {
        super(Warehouse.class, new Class[]{});
    }

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        // System.out.println("🔍 WarehouseSnapshotGenerator.getPriority() called for " + objectType.getSimpleName() + 
        //                  " on " + database.getClass().getSimpleName());
        
        if (database instanceof SnowflakeDatabase) {
            // Primary responsibility: Warehouse objects  
            if (Warehouse.class.isAssignableFrom(objectType)) {
                // System.out.println("✅ WarehouseSnapshotGenerator returning PRIORITY_DATABASE for Warehouse on Snowflake");
                return PRIORITY_DATABASE;
            }
            
            // Additional responsibility: Child discovery for Account objects (since we addsTo Account)
            if (addsTo() != null) {
                for (Class<? extends DatabaseObject> addType : addsTo()) {
                    if (addType.isAssignableFrom(objectType)) {
                        // System.out.println("✅ WarehouseSnapshotGenerator returning PRIORITY_ADDITIONAL for " + objectType.getSimpleName() + " (addsTo)");
                        return PRIORITY_ADDITIONAL;
                    }
                }
            }
        }
        
        // System.out.println("❌ WarehouseSnapshotGenerator returning PRIORITY_NONE for " + objectType.getSimpleName());
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] addsTo() {
        // Warehouses are stored in Account objects but also need to be accessible at top-level for diff operations
        return new Class[] { Account.class };
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) 
            throws DatabaseException, InvalidExampleException {
        
        if (example == null) {
            return null;
        }
        
        if (!(example instanceof Warehouse)) {
            return null;
        }
        
        Warehouse exampleWarehouse = (Warehouse) example;
        String warehouseName = exampleWarehouse.getName();
        
        if (warehouseName == null) {
            return null;
        }
        
        Database database = snapshot.getDatabase();
        if (!(database instanceof SnowflakeDatabase)) {
            return null;
        }
        
        try {
            return snapshotSingleWarehouse(warehouseName, database);
        } catch (SQLException e) {
            throw new DatabaseException("Error querying warehouse information for " + warehouseName + ": " + e.getMessage(), e);
        }
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) 
            throws DatabaseException, InvalidExampleException {
        
        logger.fine("WarehouseSnapshotGenerator.addTo() called with foundObject: " + 
                   (foundObject != null ? foundObject.getClass().getSimpleName() + 
                    " name=" + (foundObject instanceof Account ? ((Account)foundObject).getName() : "N/A") : "null"));
        
        if (!snapshot.getSnapshotControl().shouldInclude(Warehouse.class)) {
            logger.fine("WarehouseSnapshotGenerator: Warehouse class not included in snapshot control");
            return;
        }

        if (foundObject instanceof Account) {
            Account account = (Account) foundObject;
            Database database = snapshot.getDatabase();
            
            logger.fine("WarehouseSnapshotGenerator: Processing Account '" + account.getName() + "' for warehouse discovery");
            
            if (!(database instanceof SnowflakeDatabase)) {
                logger.fine("WarehouseSnapshotGenerator: Database is not Snowflake");
                return;
            }
            
            try {
                logger.fine("WarehouseSnapshotGenerator: Starting bulk warehouse discovery for account");
                addAllWarehouses(account, database, snapshot);
                logger.fine("WarehouseSnapshotGenerator: Completed warehouse discovery for account");
            } catch (SQLException e) {
                logger.warning("WarehouseSnapshotGenerator: Error discovering warehouses: " + e.getMessage());
                throw new DatabaseException("Error discovering warehouses: " + e.getMessage(), e);
            }
        } else {
            logger.fine("WarehouseSnapshotGenerator: foundObject is not Account (" + 
                             (foundObject != null ? foundObject.getClass().getSimpleName() : "null") + ")");
        }
    }

    /**
     * Snapshots a single warehouse with complete attribute coverage.
     */
    private Warehouse snapshotSingleWarehouse(String warehouseName, Database database) 
            throws SQLException, DatabaseException {
        
        // Use SHOW WAREHOUSES for comprehensive warehouse information
        String sql = "SHOW WAREHOUSES LIKE ?";
        PreparedStatement stmt = ((JdbcConnection) database.getConnection()).prepareStatement(sql);
        stmt.setString(1, warehouseName);
        ResultSet rs = stmt.executeQuery();
        
        if (!rs.next()) {
            rs.close();
            stmt.close();
            return null; // Warehouse doesn't exist
        }
        
        // Create warehouse object with full attribute coverage
        Warehouse result = new Warehouse();
        result.setName(rs.getString("name"));
        
        // Configuration properties (included in diffs)
        String type = rs.getString("type");
        if (type != null) {
            result.setType(type);
        }
        
        String size = rs.getString("size");
        if (size != null) {
            result.setSize(size);
        }
        
        int minClusterCount = rs.getInt("min_cluster_count");
        if (!rs.wasNull()) {
            result.setMinClusterCount(minClusterCount);
        }
        
        int maxClusterCount = rs.getInt("max_cluster_count");
        if (!rs.wasNull()) {
            result.setMaxClusterCount(maxClusterCount);
        }
        
        int autoSuspend = rs.getInt("auto_suspend");
        if (!rs.wasNull()) {
            result.setAutoSuspend(autoSuspend);
        }
        
        String autoResume = rs.getString("auto_resume");
        if (autoResume != null) {
            result.setAutoResume(convertYesNoToBoolean(autoResume));
        }
        
        String resourceMonitor = rs.getString("resource_monitor");
        if (resourceMonitor != null && !"null".equalsIgnoreCase(resourceMonitor)) {
            result.setResourceMonitor(resourceMonitor);
        }
        
        String comment = rs.getString("comment");
        if (comment != null && !"null".equalsIgnoreCase(comment)) {
            result.setComment(comment);
        }
        
        String enableQueryAcceleration = rs.getString("enable_query_acceleration");
        Boolean accelerationEnabled = null;
        if (enableQueryAcceleration != null) {
            accelerationEnabled = convertYesNoToBoolean(enableQueryAcceleration);
            result.setEnableQueryAcceleration(accelerationEnabled);
        }
        
        int queryAccelerationMaxScaleFactor = rs.getInt("query_acceleration_max_scale_factor");
        if (!rs.wasNull()) {
            // Only capture scale factor if acceleration is enabled or not explicitly disabled
            // This prevents capturing Snowflake defaults that would violate our validation
            if (accelerationEnabled == null || accelerationEnabled.booleanValue()) {
                result.setQueryAccelerationMaxScaleFactor(queryAccelerationMaxScaleFactor);
            }
            // If acceleration is explicitly false but scale factor > 0, don't capture the scale factor
            // to avoid generating invalid changelogs
        }
        
        String scalingPolicy = rs.getString("scaling_policy");
        if (scalingPolicy != null) {
            result.setScalingPolicy(scalingPolicy);
        }
        
        // State properties (excluded from diff, snapshot only)
        String state = rs.getString("state");
        if (state != null) {
            result.setState(state);
        }
        
        int startedClusters = rs.getInt("started_clusters");
        if (!rs.wasNull()) {
            result.setStartedClusters(startedClusters);
        }
        
        int running = rs.getInt("running");
        if (!rs.wasNull()) {
            result.setRunning(running);
        }
        
        int queued = rs.getInt("queued");
        if (!rs.wasNull()) {
            result.setQueued(queued);
        }
        
        String isDefault = rs.getString("is_default");
        if (isDefault != null) {
            result.setIsDefault(convertYesNoToBoolean(isDefault));
        }
        
        String isCurrent = rs.getString("is_current");
        if (isCurrent != null) {
            result.setIsCurrent(convertYesNoToBoolean(isCurrent));
        }
        
        // Handle float columns that may contain "." instead of numeric values
        try {
            String availableStr = rs.getString("available");
            if (availableStr != null && !availableStr.equals(".") && !availableStr.trim().isEmpty()) {
                result.setAvailable(Float.parseFloat(availableStr));
            }
        } catch (NumberFormatException e) {
            // Ignore invalid float values
        }
        
        try {
            String provisioningStr = rs.getString("provisioning");
            if (provisioningStr != null && !provisioningStr.equals(".") && !provisioningStr.trim().isEmpty()) {
                result.setProvisioning(Float.parseFloat(provisioningStr));
            }
        } catch (NumberFormatException e) {
            // Ignore invalid float values
        }
        
        try {
            String quiescingStr = rs.getString("quiescing");
            if (quiescingStr != null && !quiescingStr.equals(".") && !quiescingStr.trim().isEmpty()) {
                result.setQuiescing(Float.parseFloat(quiescingStr));
            }
        } catch (NumberFormatException e) {
            // Ignore invalid float values
        }
        
        try {
            String otherStr = rs.getString("other");
            if (otherStr != null && !otherStr.equals(".") && !otherStr.trim().isEmpty()) {
                result.setOther(Float.parseFloat(otherStr));
            }
        } catch (NumberFormatException e) {
            // Ignore invalid float values
        }
        
        Timestamp createdOn = rs.getTimestamp("created_on");
        if (createdOn != null) {
            result.setCreatedOn(new Date(createdOn.getTime()));
        }
        
        Timestamp resumedOn = rs.getTimestamp("resumed_on");
        if (resumedOn != null) {
            result.setResumedOn(new Date(resumedOn.getTime()));
        }
        
        Timestamp updatedOn = rs.getTimestamp("updated_on");
        if (updatedOn != null) {
            result.setUpdatedOn(new Date(updatedOn.getTime()));
        }
        
        String owner = rs.getString("owner");
        if (owner != null) {
            result.setOwner(owner);
        }
        
        String ownerRoleType = rs.getString("owner_role_type");
        if (ownerRoleType != null) {
            result.setOwnerRoleType(ownerRoleType);
        }
        
        rs.close();
        stmt.close();
        
        return result;
    }

    /**
     * Bulk warehouse discovery for account-level snapshots.
     */
    private void addAllWarehouses(Account account, Database database, DatabaseSnapshot snapshot) throws SQLException, DatabaseException {
        logger.fine("WarehouseSnapshotGenerator: Executing SHOW WAREHOUSES query");
        
        Statement stmt = ((JdbcConnection) database.getConnection()).createStatement();
        ResultSet rs = stmt.executeQuery("SHOW WAREHOUSES");
        
        int warehouseCount = 0;
        while (rs.next()) {
            String warehouseName = rs.getString("name");
            warehouseCount++;
            
            logger.fine("WarehouseSnapshotGenerator: Found warehouse #" + warehouseCount + ": " + warehouseName);
            
            // Create warehouse object for each discovered warehouse
            Warehouse warehouseObject = new Warehouse();
            warehouseObject.setName(warehouseName);
            
            // Set comprehensive attributes from SHOW WAREHOUSES
            String type = rs.getString("type");
            if (type != null) {
                warehouseObject.setType(type);
            }
            
            String size = rs.getString("size");
            if (size != null) {
                warehouseObject.setSize(size);
            }
            
            // Configuration properties needed for testing and diff operations
            int autoSuspend = rs.getInt("auto_suspend");
            if (!rs.wasNull()) {
                warehouseObject.setAutoSuspend(autoSuspend);
            }
            
            String autoResume = rs.getString("auto_resume");
            if (autoResume != null) {
                warehouseObject.setAutoResume(convertYesNoToBoolean(autoResume));
            }
            
            int minClusterCount = rs.getInt("min_cluster_count");
            if (!rs.wasNull()) {
                warehouseObject.setMinClusterCount(minClusterCount);
            }
            
            int maxClusterCount = rs.getInt("max_cluster_count");
            if (!rs.wasNull()) {
                warehouseObject.setMaxClusterCount(maxClusterCount);
            }
            
            String resourceMonitor = rs.getString("resource_monitor");
            if (resourceMonitor != null && !"null".equalsIgnoreCase(resourceMonitor)) {
                warehouseObject.setResourceMonitor(resourceMonitor);
            }
            
            // State properties 
            String state = rs.getString("state");
            if (state != null) {
                warehouseObject.setState(state);
            }
            
            String comment = rs.getString("comment");
            if (comment != null && !"null".equalsIgnoreCase(comment)) {
                warehouseObject.setComment(comment);
            }
            
            logger.fine("WarehouseSnapshotGenerator: Adding warehouse '" + warehouseName + "' to account '" + account.getName() + "'");
            account.addDatabaseObject(warehouseObject);
            
            // CRITICAL FIX: Also add warehouse to top-level snapshot for diff access
            // This enables snapshot.get(Warehouse.class) to find warehouses for changelog generation
            try {
                logger.fine("WarehouseSnapshotGenerator: Adding warehouse '" + warehouseName + "' to top-level snapshot for diff access");
                snapshot.include(warehouseObject);
            } catch (InvalidExampleException e) {
                logger.warning("WarehouseSnapshotGenerator: Could not add warehouse to top-level snapshot: " + e.getMessage());
            }
        }
        
        logger.fine("WarehouseSnapshotGenerator: Completed discovery - found " + warehouseCount + " warehouses");
        
        rs.close();
        stmt.close();
    }

    /**
     * Converts Snowflake boolean-like strings to Boolean objects.
     * Handles YES/NO, Y/N, true/false, TRUE/FALSE patterns.
     */
    private Boolean convertYesNoToBoolean(String yesNoValue) {
        if (yesNoValue == null) {
            return null;
        }
        String trimmed = yesNoValue.trim().toUpperCase();
        if ("YES".equals(trimmed) || "Y".equals(trimmed) || "TRUE".equals(trimmed)) {
            return Boolean.TRUE;
        } else if ("NO".equals(trimmed) || "N".equals(trimmed) || "FALSE".equals(trimmed)) {
            return Boolean.FALSE;
        }
        return null;
    }

    @Override
    public Class<? extends SnapshotGenerator>[] replaces() {
        return new Class[0];
    }
}